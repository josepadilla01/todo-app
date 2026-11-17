const loginView = document.getElementById('loginView');
const todoView = document.getElementById('todoView');
const loginError = document.getElementById('loginError');
const todoForm = document.getElementById('todoForm');
const todoInput = document.getElementById('todoInput');
const todoList = document.getElementById('todoList');
const welcomeMessage = document.getElementById('welcomeMessage');
const logoutCsrfToken = document.getElementById('logoutCsrfToken');

const state = {
  authenticated: false,
  csrfToken: null,
};

async function apiRequest(url, options = {}) {
  const { headers: optionHeaders = {}, ...requestOptions } = options;
  const method = requestOptions.method || 'GET';
  const response = await fetch(url, {
    ...requestOptions,
    headers: {
      'Content-Type': 'application/json',
      ...(method !== 'GET' && state.csrfToken
        ? { 'X-XSRF-TOKEN': state.csrfToken }
        : {}),
      ...optionHeaders,
    },
    credentials: 'same-origin',
  });

  if (response.status === 401) {
    setView(false);
    throw new Error('Your session has expired. Please sign in again.');
  }

  const contentType = response.headers.get('content-type') || '';
  const data = contentType.includes('application/json') ? await response.json() : null;

  if (!response.ok) {
    throw new Error((data && data.message) || 'Request failed.');
  }

  return data;
}

function setView(isAuthenticated, username = '') {
  state.authenticated = isAuthenticated;
  loginView.classList.toggle('hidden', isAuthenticated);
  todoView.classList.toggle('hidden', !isAuthenticated);
  welcomeMessage.textContent = isAuthenticated ? `Signed in as ${username}` : '';

  if (!isAuthenticated) {
    todoList.replaceChildren();
  }
}

function renderTodos(items) {
  todoList.replaceChildren();

  if (!items.length) {
    const emptyItem = document.createElement('li');
    emptyItem.className = 'todo-item';
    const emptyText = document.createElement('span');
    emptyText.className = 'todo-text';
    emptyText.textContent = 'No tasks yet.';
    emptyItem.appendChild(emptyText);
    todoList.appendChild(emptyItem);
    return;
  }

  items.forEach((todo) => {
    const item = document.createElement('li');
    item.className = `todo-item ${todo.done ? 'completed' : ''}`;

    const main = document.createElement('div');
    main.className = 'todo-main';
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.className = 'checkbox';
    checkbox.checked = todo.done;
    const text = document.createElement('span');
    text.className = 'todo-text';
    text.textContent = todo.text;
    main.append(checkbox, text);

    const actions = document.createElement('div');
    actions.className = 'todo-actions';
    const doneBtn = document.createElement('button');
    doneBtn.className = 'complete';
    doneBtn.type = 'button';
    doneBtn.textContent = todo.done ? 'Undo' : 'Done';
    const updateBtn = document.createElement('button');
    updateBtn.className = 'secondary update';
    updateBtn.type = 'button';
    updateBtn.textContent = 'Update';
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete';
    deleteBtn.type = 'button';
    deleteBtn.textContent = 'Delete';
    actions.append(doneBtn, updateBtn, deleteBtn);
    item.append(main, actions);

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

async function loadAuthState() {
  try {
    const auth = await apiRequest('/api/auth');
    state.csrfToken = auth.csrfToken;
    logoutCsrfToken.name = auth.csrfParameterName;
    logoutCsrfToken.value = auth.csrfToken;
    setView(auth.authenticated, auth.username || '');
    loginError.textContent = '';
    if (auth.authenticated) {
      await loadTodos();
    }
  } catch (error) {
    loginError.textContent = error.message;
  }
}

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

setView(false);
loadAuthState();
