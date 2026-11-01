import { useEffect, useState } from 'react'
import { api, loadAllTickets } from '../api'
import { BubbleField } from '../components/BubbleField'
import { Confirm, EmptyState, ErrorState, Loader, PageHeader } from '../components/Common'
import { TicketDialog } from '../components/TicketDialog'
import { useAsync } from '../hooks/useAsync'
import { truncate } from '../utils'

export function VisualizationPage({ version, onChanged }) {
  const result = useAsync(loadAllTickets, [version])
  const [selected, setSelected] = useState(null)
  const [edit, setEdit] = useState(null)
  const [confirmDelete, setConfirmDelete] = useState(null)
  const tickets = result.data || []

  useEffect(() => {
    if (!result.data) return
    const exists = (value) => !value || result.data.some((ticket) => ticket.id === value.id)
    setSelected((value) => (exists(value) ? value : null))
    setEdit((value) => (exists(value) ? value : null))
    setConfirmDelete((value) => (exists(value) ? value : null))
  }, [result.data])

  const remove = async () => {
    try {
      await api.delete(`/tickets/${confirmDelete.id}?version=${confirmDelete.version}`)
      setConfirmDelete(null)
      setSelected(null)
      onChanged('Билет удалён из базы данных')
    } catch (error) {
      setConfirmDelete({ ...confirmDelete, error: error.message })
    }
  }

  return (
    <section className="page">
      <PageHeader
        title="Визуализация билетов"
        description="Чем выше цена, тем больше пузырь. Чем больше скидка, тем насыщеннее его прозрачная оранжевая заливка."
      />
      <div className="legend panel">
        <span>
          <i className="legend-bubble small" /> низкая цена
        </span>
        <span>
          <i className="legend-bubble large" /> высокая цена
        </span>
        <span>
          <i className="legend-discount" /> интенсивность — скидка
        </span>
        <span>Один клик — выбрать, двойной — изменить</span>
      </div>
      {result.loading ? (
        <Loader />
      ) : result.error ? (
        <ErrorState message={result.error} />
      ) : !tickets.length ? (
        <div className="panel">
          <EmptyState title="Визуализировать пока нечего" text="Создайте хотя бы один билет." />
        </div>
      ) : (
        <BubbleField
          tickets={tickets}
          selectedId={selected?.id}
          onSelect={setSelected}
          onEdit={setEdit}
        />
      )}
      {selected && (
        <div className="selection-bar">
          <div>
            <span>Выбран билет #{selected.id}</span>
            <strong>{truncate(selected.name, 48)}</strong>
          </div>
          <button className="button light" onClick={() => setEdit(selected)}>
            Изменить
          </button>
          <button className="button danger" onClick={() => setConfirmDelete(selected)}>
            Удалить
          </button>
          <button className="close-selection" onClick={() => setSelected(null)}>
            ×
          </button>
        </div>
      )}
      {edit && (
        <TicketDialog
          mode="edit"
          ticket={edit}
          onClose={() => setEdit(null)}
          onSaved={() => {
            setEdit(null)
            setSelected(null)
            onChanged('Билет изменён')
          }}
        />
      )}
      {confirmDelete && (
        <Confirm
          title={`Удалить «${truncate(confirmDelete.name, 60)}»?`}
          text="Объект исчезнет из таблицы и визуализации у всех подключённых клиентов."
          error={confirmDelete.error}
          onCancel={() => setConfirmDelete(null)}
          onConfirm={remove}
        />
      )}
    </section>
  )
}
