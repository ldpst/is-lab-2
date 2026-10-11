import { EmptyState } from '../Common'
import { formatDate } from '../../utils'

const STATUS = {
  IN_PROGRESS: 'Выполняется',
  SUCCESS: 'Успешно',
  FAILED: 'Ошибка',
}

export function ImportHistoryTable({ operations }) {
  if (!operations.length) {
    return (
      <EmptyState
        title="Импортов пока нет"
        text="Здесь появятся выполненные вами операции импорта."
      />
    )
  }

  return (
    <div className="table-scroll">
      <table className="import-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Статус</th>
            <th>Пользователь</th>
            <th>Файл</th>
            <th>Начало</th>
            <th>Добавлено</th>
            <th>Сообщение</th>
          </tr>
        </thead>
        <tbody>
          {operations.map((operation) => (
            <tr key={operation.id}>
              <td className="mono">#{operation.id}</td>
              <td>
                <span className={`import-status ${operation.status.toLowerCase()}`}>
                  {STATUS[operation.status]}
                </span>
              </td>
              <td>{operation.username}</td>
              <td>{operation.fileName}</td>
              <td>{formatDate(operation.createdAt)}</td>
              <td>{operation.status === 'SUCCESS' ? operation.importedCount : '—'}</td>
              <td className="import-error" title={operation.errorMessage || ''}>
                {operation.errorMessage || '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
