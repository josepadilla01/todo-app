import { useEffect, useState } from 'react'
import { request, SessionExpiredError } from './api.js'

export default function App() {
  const [auth, setAuth] = useState(null)
  const [todos, setTodos] = useState([])
  const [text, setText] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function initialize() {
      try {
        const session = await request('/api/auth', { signal: controller.signal })
        if (controller.signal.aborted) return
        if (session.authenticated) {
          const items = await request('/api/todos', { signal: controller.signal })
          if (!controller.signal.aborted) setTodos(items)
        }
        if (!controller.signal.aborted) setAuth(session)
      } catch (err) {
        if (!controller.signal.aborted) {
          if (err instanceof SessionExpiredError) {
            setAuth({ authenticated: false })
          }
          setError(err.message)
        }
      }
    }

    initialize()
    return () => controller.abort()
  }, [])

  async function changeTodo(method, path, body) {
    setBusy(true)
    setError('')
    try {
      const todo = await request(path, {
        method,
        csrfToken: auth.csrfToken,
        ...(body ? { body: JSON.stringify(body) } : {}),
      })
      setTodos((items) => {
        if (method === 'POST') return [...items, todo]
        if (method === 'DELETE') return items.filter((item) => item.id !== todo.id)
        return items.map((item) => item.id === todo.id ? todo : item)
      })
      return true
    } catch (err) {
      if (err instanceof SessionExpiredError) {
        setAuth({ authenticated: false })
        setTodos([])
      }
      setError(err.message)
      return false
    } finally {
      setBusy(false)
    }
  }

  async function addTodo(event) {
    event.preventDefault()
    const trimmed = text.trim()
    if (!trimmed) return
    if (await changeTodo('POST', '/api/todos', { text: trimmed })) setText('')
  }

  function editTodo(todo) {
    const updated = window.prompt('Update task', todo.text)
    if (updated?.trim()) {
      changeTodo('PUT', `/api/todos/${todo.id}`, { text: updated.trim() })
    }
  }

  if (!auth) {
    return <main className="app-shell"><section className="card">
      {error ? <p className="error-message" role="alert">{error}</p> : <p>Loading...</p>}
    </section></main>
  }

  return (
    <main className="app-shell">
      {!auth.authenticated ? (
        <section className="card">
          <h1>Welcome to To-Do</h1>
          <p>Sign in or create an account with Amazon Cognito to manage your tasks.</p>
          <a className="button" href="/oauth2/authorization/cognito">Sign In / Create Account</a>
          {error && <p className="error-message" role="alert">{error}</p>}
        </section>
      ) : (
        <section className="card">
          <header className="todo-header">
            <div>
              <h1>To-Do List</h1>
              <p className="muted">Signed in as {auth.username}</p>
            </div>
            <form action="/logout" method="post">
              <input type="hidden" name={auth.csrfParameterName} value={auth.csrfToken} />
              <button type="submit" className="secondary">Log Out</button>
            </form>
          </header>
          <form className="todo-form" onSubmit={addTodo}>
            <input aria-label="New task" value={text} onChange={(event) => setText(event.target.value)}
              placeholder="Add a new task" required />
            <button type="submit" disabled={busy}>Add</button>
          </form>
          {error && <p className="error-message" role="alert">{error}</p>}
          <ul className="todo-list">
            {todos.length === 0 ? <li className="todo-item">No tasks yet.</li> : todos.map((todo) => (
              <li key={todo.id} className={`todo-item ${todo.done ? 'completed' : ''}`}>
                <div className="todo-main">
                  <input type="checkbox" className="checkbox" checked={todo.done}
                    aria-label={`Mark ${todo.text} ${todo.done ? 'incomplete' : 'complete'}`}
                    disabled={busy}
                    onChange={(event) => changeTodo('PUT', `/api/todos/${todo.id}`, { done: event.target.checked })} />
                  <span className="todo-text">{todo.text}</span>
                </div>
                <div className="todo-actions">
                  <button type="button" className="complete" disabled={busy}
                    onClick={() => changeTodo('PUT', `/api/todos/${todo.id}`, { done: !todo.done })}>
                    {todo.done ? 'Undo' : 'Done'}
                  </button>
                  <button type="button" className="secondary" disabled={busy} onClick={() => editTodo(todo)}>Update</button>
                  <button type="button" className="delete" disabled={busy}
                    onClick={() => changeTodo('DELETE', `/api/todos/${todo.id}`)}>Delete</button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </main>
  )
}
