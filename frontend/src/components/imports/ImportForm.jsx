import { useState } from 'react'
import { api, apiUrl } from '../../api'

export function ImportForm({ onFinished }) {
  const [file, setFile] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [inputKey, setInputKey] = useState(0)

  const submit = async (event) => {
    event.preventDefault()
    if (!file) {
      setError('Выберите CSV-файл')
      return
    }

    setBusy(true)
    setError('')
    const data = new FormData()
    data.append('file', file)

    try {
      const result = await api.upload('/imports', data)
      setFile(null)
      setInputKey((value) => value + 1)
      onFinished(`Импорт #${result.operationId} завершён`)
    } catch (failure) {
      setError(failure.message)
      onFinished('Импорт отменён', false)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="panel import-form" onSubmit={submit}>
      <div>
        <h2>Загрузить билеты</h2>
        <p>Один билет — одна строка вместе со всеми полями человека, события и площадки.</p>
      </div>
      <label className="file-picker">
        <span>{file ? file.name : 'Выберите файл .csv'}</span>
        <input
          key={inputKey}
          type="file"
          accept=".csv,text/csv"
          onChange={(event) => {
            setFile(event.target.files?.[0] || null)
            setError('')
          }}
        />
      </label>
      <div className="import-actions">
        <a className="button ghost" href={apiUrl('/imports/template')} download>
          Скачать шаблон
        </a>
        <button className="button primary" disabled={busy}>
          {busy ? 'Импорт…' : 'Импортировать'}
        </button>
      </div>
      {error && <div className="form-error">{error}</div>}
    </form>
  )
}
