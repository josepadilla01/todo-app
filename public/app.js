const loginView = document.getElementById('loginView');
const todoView = document.getElementById('todoView');
const loginForm = document.getElementById('loginForm');
const loginError = document.getElementById('loginError');
const todoForm = document.getElementById('todoForm');
const todoInput = document.getElementById('todoInput');
const todoList = document.getElementById('todoList');
const logoutBtn = document.getElementById('logoutBtn');

const state = {
  authenticated: false,
};

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'Request failed.');
  }

  return data;
}

function setView(isAuthenticated) {
  state.authenticated = isAuthenticated;
  loginView.classList.toggle('hidden', isAuthenticated);
  todoView.classList.toggle('hidden', !isAuthenticated);

  if (!isAuthenticated) {
    loginForm.reset();
    loginError.textContent = '';
  }
}

function renderTodos(items) {
  todoList.innerHTML = '';

  if (!items.length) {
    const emptyItem = document.createElement('li');
    emptyItem.className = 'todo-item';
    emptyItem.innerHTML = '<span class="todo-text">No tasks yet.</span>';
    todoList.appendChild(emptyItem);
    return;
  }

  items.forEach((todo) => {
    const item = document.createElement('li');
    item.className = `todo-item ${todo.done ? 'completed' : ''}`;

    item.innerHTML = `
      <div class="todo-main">
        <input type="checkbox" class="checkbox" ${todo.done ? 'checked' : ''} />
        <span class="todo-text">${todo.text}</span>
      </div>
      <div class="todo-actions">
        <button class="complete" type="button">${todo.done ? 'Undo' : 'Done'}</button>
        <button class="secondary update" type="button">Update</button>
        <button class="delete" type="button">Delete</button>
      </div>
    `;

    const checkbox = item.querySelector('.checkbox');
    const doneBtn = item.querySelector('.complete');
    const updateBtn = item.querySelector('.update');
    const deleteBtn = item.querySelector('.delete');

    checkbox.addEventListener('change', () => toggleTodo(todo.id, checkbox.checked));
    doneBtn.addEventListener('click', () => toggleTodo(todo.id, !todo.done));
    updateBtn.addEventListener('click', () => updateTodo(todo.id, todo.text));
    deleteBtn.addEventListener('click', () => deleteTodo(todo.id));

    todoList.appendChild(item);
  });
}

async function loadTodos() {
  try {
    const todos = await apiRequest('/api/todos');
    renderTodos(todos);
  } catch (error) {
    console.error(error);
  }
}

loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();

  const username = document.getElementById('username').value;
  const password = document.getElementById('password').value;

  try {
    await apiRequest('/api/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });

    setView(true);
    loadTodos();
  } catch (error) {
    loginError.textContent = error.message;
  }
});

todoForm.addEventListener('submit', async (event) => {
  event.preventDefault();

  const text = todoInput.value.trim();
  if (!text) {
    return;
  }

  try {
    await apiRequest('/api/todos', {
      method: 'POST',
      body: JSON.stringify({ text }),
    });

    todoInput.value = '';
    loadTodos();
  } catch (error) {
    console.error(error);
  }
});

async function toggleTodo(id, done) {
  try {
    await apiRequest(`/api/todos/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ done }),
    });
    loadTodos();
  } catch (error) {
    console.error(error);
  }
}

async function updateTodo(id, currentText) {
  const updatedText = window.prompt('Update task', currentText);

  if (updatedText === null) {
    return;
  }

  const trimmedText = updatedText.trim();
  if (!trimmedText) {
    return;
  }

  try {
    await apiRequest(`/api/todos/${id}`, {
      method: 'PUT',
      body: JSON.stringify({ text: trimmedText }),
    });
    loadTodos();
  } catch (error) {
    console.error(error);
  }
}

async function deleteTodo(id) {
  try {
    await apiRequest(`/api/todos/${id}`, {
      method: 'DELETE',
    });
    loadTodos();
  } catch (error) {
    console.error(error);
  }
}

logoutBtn.addEventListener('click', () => {
  setView(false);
});

setView(false);
