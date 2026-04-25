const TASKS_STORAGE_KEY = "todo-tasks-v1";
const THEME_STORAGE_KEY = "todo-theme-v1";

const state = {
  tasks: loadTasks(),
  filter: "all"
};

const body = document.body;
const taskForm = document.getElementById("task-form");
const taskInput = document.getElementById("task-input");
const taskList = document.getElementById("task-list");
const taskTemplate = document.getElementById("task-template");
const tasksCount = document.getElementById("tasks-count");
const doneCount = document.getElementById("done-count");
const filters = document.querySelectorAll(".filter");
const clearCompletedBtn = document.getElementById("clear-completed");
const themeToggle = document.getElementById("theme-toggle");

initTheme();
render();

taskForm.addEventListener("submit", (event) => {
  event.preventDefault();

  const rawValue = taskInput.value.trim();
  if (!rawValue) return;

  const newTasks = splitTasks(rawValue).map((text) => ({
    id: crypto.randomUUID(),
    text,
    completed: false
  }));

  if (!newTasks.length) return;

  state.tasks.unshift(...newTasks);

  taskInput.value = "";
  persist();
  render();
});

filters.forEach((button) => {
  button.addEventListener("click", () => {
    state.filter = button.dataset.filter;
    filters.forEach((item) => item.classList.toggle("is-active", item === button));
    render();
  });
});

clearCompletedBtn.addEventListener("click", () => {
  state.tasks = state.tasks.filter((task) => !task.completed);
  persist();
  render();
});

themeToggle.addEventListener("change", () => {
  const theme = themeToggle.checked ? "dark" : "light";
  body.classList.remove("dark", "light");
  body.classList.add(theme);
  localStorage.setItem(THEME_STORAGE_KEY, theme);
});

function render() {
  taskList.innerHTML = "";

  const visibleTasks = state.tasks.filter((task) => {
    if (state.filter === "active") return !task.completed;
    if (state.filter === "completed") return task.completed;
    return true;
  });

  visibleTasks.forEach((task) => {
    const node = taskTemplate.content.firstElementChild.cloneNode(true);
    const checkbox = node.querySelector(".task-check");
    const text = node.querySelector(".task-text");
    const editBtn = node.querySelector(".edit-btn");
    const deleteBtn = node.querySelector(".delete-btn");

    checkbox.checked = task.completed;
    text.textContent = task.text;
    node.classList.toggle("is-completed", task.completed);

    checkbox.addEventListener("change", () => {
      task.completed = checkbox.checked;
      persist();
      render();
    });

    editBtn.addEventListener("click", () => {
      const updated = window.prompt("Edit task", task.text);
      if (updated === null) return;

      const nextText = updated.trim();
      if (!nextText) return;

      task.text = nextText;
      persist();
      render();
    });

    deleteBtn.addEventListener("click", () => {
      state.tasks = state.tasks.filter((item) => item.id !== task.id);
      persist();
      render();
    });

    taskList.appendChild(node);
  });

  if (!visibleTasks.length) {
    const emptyState = document.createElement("li");
    emptyState.className = "task-item";
    emptyState.innerHTML = '<div class="task-text">No tasks yet. Add your first one above.</div>';
    taskList.appendChild(emptyState);
  }

  tasksCount.textContent = String(state.tasks.length);
  doneCount.textContent = String(state.tasks.filter((task) => task.completed).length);
  clearCompletedBtn.disabled = !state.tasks.some((task) => task.completed);
  clearCompletedBtn.style.opacity = clearCompletedBtn.disabled ? "0.55" : "1";
}

function persist() {
  localStorage.setItem(TASKS_STORAGE_KEY, JSON.stringify(state.tasks));
}

function loadTasks() {
  try {
    const stored = JSON.parse(localStorage.getItem(TASKS_STORAGE_KEY));
    return Array.isArray(stored) ? stored : [];
  } catch {
    return [];
  }
}

function initTheme() {
  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) || "dark";
  body.classList.add(savedTheme);
  themeToggle.checked = savedTheme === "dark";
}

function splitTasks(value) {
  if (value.includes(", ")) {
    return value
      .split(", ")
      .map((item) => item.trim())
      .filter(Boolean);
  }

  return [value];
}
