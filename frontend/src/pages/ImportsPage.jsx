import { useState } from 'react'
import { api } from '../api'
import { ErrorState, Loader, PageHeader } from '../components/Common'
import { ImportForm } from '../components/imports/ImportForm'
import { ImportHistoryTable } from '../components/imports/ImportHistoryTable'
import { useAsync } from '../hooks/useAsync'

export function ImportsPage({ version, onChanged }) {
  const [localVersion, setLocalVersion] = useState(0)
  const history = useAsync(() => api.get('/imports'), [version, localVersion])

  const finished = (message, successful = true) => {
    setLocalVersion((value) => value + 1)
    if (successful) onChanged(message)
  }

  return (
    <section className="page">
      <PageHeader title="Импорт" />
      <ImportForm onFinished={finished} />
      <div className="panel import-history">
        <div className="import-history-heading">
          <h2>История импорта</h2>
        </div>
        {history.loading ? (
          <Loader />
        ) : history.error ? (
          <ErrorState message={history.error} />
        ) : (
          <ImportHistoryTable operations={history.data} />
        )}
      </div>
    </section>
  )
}
