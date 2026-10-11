import { useState } from 'react'
import { api } from '../api'

export function AuthPage({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')
    setMessage('')

    try {
      if (mode === 'register') {
        await api.auth.register(username, password)
        setMode('login')
        setPassword('')
        setMessage('Пользователь создан. Теперь войдите в систему.')
      } else {
        await api.auth.login(username, password)
        onAuthenticated(await api.auth.me())
      }
    } catch (failure) {
      setError(failure.message)
    } finally {
      setBusy(false)
    }
  }

  const changeMode = (nextMode) => {
    setMode(nextMode)
    setError('')
    setMessage('')
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <div className="auth-brand">
          <span className="brand-mark">T</span>
          <div>
            <strong>Ticket System</strong>
            <span>Лабораторная работа №2</span>
          </div>
        </div>
        <div className="auth-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => changeMode('login')}>
            Вход
          </button>
          <button
            className={mode === 'register' ? 'active' : ''}
            onClick={() => changeMode('register')}
          >
            Регистрация
          </button>
        </div>
        <form onSubmit={submit}>
          <label>
            <span>Имя пользователя</span>
            <input
              autoFocus
              autoComplete="username"
              minLength="3"
              maxLength="50"
              pattern="[a-zA-Z0-9._-]+"
              required
              value={username}
              onChange={(event) => setUsername(event.target.value)}
            />
          </label>
          <label>
            <span>Пароль</span>
            <input
              type="password"
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              minLength="8"
              maxLength="100"
              required
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          {message && <div className="auth-message">{message}</div>}
          {error && <div className="form-error">{error}</div>}
          <button className="button primary" disabled={busy}>
            {busy ? 'Подождите…' : mode === 'login' ? 'Войти' : 'Создать пользователя'}
          </button>
        </form>
      </section>
    </main>
  )
}
