import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, test, vi } from 'vitest'
import App from './App.jsx'

function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

test('offers Cognito sign-in for an anonymous session', async () => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ authenticated: false })))
  render(<App />)
  expect(await screen.findByRole('link', { name: /sign in/i }))
    .toHaveAttribute('href', '/oauth2/authorization/cognito')
})

test('renders tasks safely and sends the CSRF token when creating a task', async () => {
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      authenticated: true, username: 'Alice', csrfToken: 'test-csrf', csrfParameterName: '_csrf',
    }))
    .mockResolvedValueOnce(jsonResponse([{ id: 1, text: '<img src=x>', done: false }]))
    .mockResolvedValueOnce(jsonResponse({ id: 2, text: 'New task', done: false }, 201))
  vi.stubGlobal('fetch', fetchMock)
  render(<App />)
  expect(await screen.findByText('<img src=x>')).toBeInTheDocument()
  expect(document.querySelector('img')).not.toBeInTheDocument()
  await userEvent.type(screen.getByRole('textbox', { name: 'New task' }), 'New task')
  await userEvent.click(screen.getByRole('button', { name: 'Add' }))
  expect(await screen.findByText('New task')).toBeInTheDocument()
  expect(fetchMock.mock.calls[2][1].headers['X-XSRF-TOKEN']).toBe('test-csrf')
  expect(fetchMock.mock.calls[2][1].body).toBe('{"text":"New task"}')
  expect(document.querySelector('input[name="_csrf"]')).toHaveValue('test-csrf')
})

test('returns to sign-in when a session expires during a mutation', async () => {
  vi.stubGlobal('fetch', vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      authenticated: true, username: 'Alice', csrfToken: 'test-csrf', csrfParameterName: '_csrf',
    }))
    .mockResolvedValueOnce(jsonResponse([{ id: 1, text: 'Task', done: false }]))
    .mockResolvedValueOnce(jsonResponse({ message: 'Unauthorized' }, 401)))
  render(<App />)
  await screen.findByText('Task')
  await userEvent.click(screen.getByRole('button', { name: 'Delete' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('session has expired')
  await waitFor(() => expect(screen.getByRole('link', { name: /sign in/i })).toBeInTheDocument())
  expect(screen.queryByText('Task')).not.toBeInTheDocument()
})

test('updates and deletes a task using the server responses', async () => {
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      authenticated: true, username: 'Alice', csrfToken: 'test-csrf', csrfParameterName: '_csrf',
    }))
    .mockResolvedValueOnce(jsonResponse([{ id: 1, text: 'Task', done: false }]))
    .mockResolvedValueOnce(jsonResponse({ id: 1, text: 'Task', done: true }))
    .mockResolvedValueOnce(jsonResponse({ id: 1, text: 'Task', done: true }))
  vi.stubGlobal('fetch', fetchMock)
  render(<App />)
  await screen.findByText('Task')
  await userEvent.click(screen.getByRole('button', { name: 'Done' }))
  expect(await screen.findByRole('checkbox', { name: 'Mark Task incomplete' })).toBeChecked()
  await userEvent.click(screen.getByRole('button', { name: 'Delete' }))
  expect(await screen.findByText('No tasks yet.')).toBeInTheDocument()
  expect(fetchMock.mock.calls[2][1].body).toBe('{"done":true}')
  expect(fetchMock.mock.calls[3][1].method).toBe('DELETE')
})

test('shows a mutation error without losing the task', async () => {
  vi.stubGlobal('fetch', vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      authenticated: true, username: 'Alice', csrfToken: 'test-csrf', csrfParameterName: '_csrf',
    }))
    .mockResolvedValueOnce(jsonResponse([{ id: 1, text: 'Task', done: false }]))
    .mockResolvedValueOnce(jsonResponse({ message: 'Save failed' }, 500)))
  render(<App />)
  await screen.findByText('Task')
  await userEvent.click(screen.getByRole('button', { name: 'Done' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('Save failed')
  expect(screen.getByRole('checkbox', { name: 'Mark Task complete' })).not.toBeChecked()
})

test('shows an error when the session endpoint is unavailable', async () => {
  vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Network unavailable')))
  render(<App />)
  expect(await screen.findByRole('alert')).toHaveTextContent('Network unavailable')
})
