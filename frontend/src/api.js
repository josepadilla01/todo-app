export class SessionExpiredError extends Error {
  constructor() {
    super('Your session has expired. Please sign in again.')
  }
}

export async function request(url, { csrfToken, headers, ...options } = {}) {
  const response = await fetch(url, {
    ...options,
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
      ...(csrfToken ? { 'X-XSRF-TOKEN': csrfToken } : {}),
      ...headers,
    },
  })

  if (response.status === 401) throw new SessionExpiredError()
  const contentType = response.headers.get('content-type') || ''
  const data = contentType.includes('application/json') ? await response.json() : null
  if (!response.ok) throw new Error(data?.message || `Request failed (${response.status}).`)
  return data
}
