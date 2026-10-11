const deploymentRoot = import.meta.env.DEV ? '' : window.location.pathname.replace(/\/[^/]*$/, '')

export const API_ROOT = `${deploymentRoot}/api`

export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message)
    this.status = status
    this.details = details
  }
}

let csrf = null

async function loadCsrf() {
  const response = await fetch(`${API_ROOT}/auth/csrf`, { credentials: 'same-origin' })
  if (!response.ok) throw new ApiError('Не удалось получить CSRF-токен', response.status)
  csrf = await response.json()
  return csrf
}

async function request(path, options = {}, canRetryCsrf = true) {
  const method = options.method || 'GET'
  const changesData = !['GET', 'HEAD', 'OPTIONS'].includes(method)
  const headers = { ...options.headers }

  if (options.body && !(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }
  if (changesData) {
    const token = csrf || (await loadCsrf())
    headers[token.headerName] = token.token
  }

  const response = await fetch(`${API_ROOT}${path}`, {
    ...options,
    headers,
    credentials: 'same-origin',
  })

  if (response.status === 403 && changesData && canRetryCsrf) {
    csrf = null
    return request(path, options, false)
  }

  if (!response.ok) {
    let body = null
    try {
      body = await response.json()
    } catch {
      body = null
    }
    const details =
      Array.isArray(body?.details) && body.details.length ? `: ${body.details.join('; ')}` : ''
    const message =
      body?.message || body?.error
        ? `${body.message || body.error}${details}`
        : `Ошибка запроса (${response.status})`
    throw new ApiError(message, response.status, body)
  }

  if (response.status === 204) return null
  return response.json()
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: 'DELETE' }),
  upload: (path, formData) =>
    request(path, {
      method: 'POST',
      body: formData,
    }),
  auth: {
    me: () => request('/auth/me'),
    register: (username, password) =>
      request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      }),
    login: async (username, password) => {
      const body = new URLSearchParams({ username, password })
      const result = await request('/auth/login', {
        method: 'POST',
        body,
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      csrf = null
      return result
    },
    logout: async () => {
      const result = await request('/auth/logout', { method: 'POST' })
      csrf = null
      return result
    },
  },
}

export function apiUrl(path) {
  return `${API_ROOT}${path}`
}

export function ticketsQuery(params) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== null && value !== undefined) query.set(key, value)
  })
  return `/tickets?${query}`
}

export async function loadAllTickets() {
  const first = await api.get(ticketsQuery({ page: 0, size: 100, sort: 'id', direction: 'asc' }))
  if (first.totalPages <= 1) return first.items
  const rest = await Promise.all(
    Array.from({ length: first.totalPages - 1 }, (_, index) =>
      api.get(ticketsQuery({ page: index + 1, size: 100, sort: 'id', direction: 'asc' })),
    ),
  )
  return [first, ...rest].flatMap((page) => page.items)
}

export function updatesUrl() {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const root = import.meta.env.DEV ? '' : deploymentRoot
  return `${protocol}//${window.location.host}${root}/updates`
}
