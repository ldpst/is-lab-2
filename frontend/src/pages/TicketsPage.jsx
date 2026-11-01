import { useState } from 'react'
import { api, ticketsQuery } from '../api'
import {
  Confirm,
  EmptyState,
  ErrorState,
  Loader,
  Modal,
  PageHeader,
  TypeBadge,
} from '../components/Common'
import { TicketDialog } from '../components/TicketDialog'
import { Pagination, Sortable, TicketDetails } from '../components/tickets/TicketTableParts'
import { useAsync } from '../hooks/useAsync'
import { formatDate, formatMoney, truncate } from '../utils'

export function TicketsPage({ version, onChanged }) {
  const [query, setQuery] = useState({
    page: 0,
    size: 10,
    sort: 'id',
    direction: 'asc',
    filterField: 'name',
  })
  const [filterDraft, setFilterDraft] = useState('')
  const [appliedFilter, setAppliedFilter] = useState('')
  const [dialog, setDialog] = useState(null)
  const [detail, setDetail] = useState(null)
  const [confirmDelete, setConfirmDelete] = useState(null)
  const result = useAsync(
    () => api.get(ticketsQuery({ ...query, filter: appliedFilter })),
    [query, appliedFilter, version],
  )

  const sortBy = (field) =>
    setQuery((old) => ({
      ...old,
      page: 0,
      sort: field,
      direction: old.sort === field && old.direction === 'asc' ? 'desc' : 'asc',
    }))
  const remove = async () => {
    try {
      await api.delete(`/tickets/${confirmDelete.id}?version=${confirmDelete.version}`)
      setConfirmDelete(null)
      onChanged('Билет удалён')
    } catch (error) {
      setConfirmDelete({ ...confirmDelete, error: error.message })
    }
  }

  return (
    <section className="page">
      <PageHeader title="Билеты" />
      <div className="toolbar panel">
        <select
          value={query.filterField}
          onChange={(event) => setQuery({ ...query, page: 0, filterField: event.target.value })}
        >
          <option value="name">Название билета</option>
          <option value="personPassportID">Passport ID</option>
          <option value="eventName">Название события</option>
          <option value="venueName">Название площадки</option>
        </select>
        <input
          value={filterDraft}
          onChange={(event) => setFilterDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter') {
              setQuery({ ...query, page: 0 })
              setAppliedFilter(filterDraft.trim())
            }
          }}
          placeholder="Неполное совпадение…"
        />
        <button
          className="button light"
          onClick={() => {
            setQuery({ ...query, page: 0 })
            setAppliedFilter(filterDraft.trim())
          }}
        >
          Найти
        </button>
        {(appliedFilter || filterDraft) && (
          <button
            className="button ghost"
            onClick={() => {
              setFilterDraft('')
              setQuery({ ...query, page: 0 })
              setAppliedFilter('')
            }}
          >
            Сбросить
          </button>
        )}
        <button
          className="button primary toolbar-create"
          onClick={() => setDialog({ mode: 'create' })}
        >
          + Новый билет
        </button>
      </div>
      <div className="panel table-panel">
        {result.loading ? (
          <Loader />
        ) : result.error ? (
          <ErrorState message={result.error} />
        ) : (
          <>
            <div className="table-scroll">
              <table>
                <thead>
                  <tr>
                    <Sortable label="ID" field="id" query={query} onSort={sortBy} />
                    <Sortable label="Название билета" field="name" query={query} onSort={sortBy} />
                    <Sortable label="Создан" field="creationDate" query={query} onSort={sortBy} />
                    <Sortable label="X" field="x" query={query} onSort={sortBy} />
                    <Sortable label="Y" field="y" query={query} onSort={sortBy} />
                    <Sortable
                      label="Passport ID"
                      field="personPassportID"
                      query={query}
                      onSort={sortBy}
                    />
                    <Sortable
                      label="Название события"
                      field="eventName"
                      query={query}
                      onSort={sortBy}
                    />
                    <Sortable label="Цена" field="price" query={query} onSort={sortBy} />
                    <Sortable label="Тип" field="type" query={query} onSort={sortBy} />
                    <Sortable label="Скидка" field="discount" query={query} onSort={sortBy} />
                    <Sortable label="Номер" field="number" query={query} onSort={sortBy} />
                    <Sortable
                      label="Название площадки"
                      field="venueName"
                      query={query}
                      onSort={sortBy}
                    />
                    <th className="actions-cell">Действия</th>
                  </tr>
                </thead>
                <tbody>
                  {result.data.items.map((ticket) => (
                    <tr key={ticket.id}>
                      <td className="mono">#{ticket.id}</td>
                      <td className="strong ticket-name-cell" title={ticket.name}>
                        {truncate(ticket.name)}
                      </td>
                      <td>{formatDate(ticket.creationDate)}</td>
                      <td>{ticket.coordinates.x}</td>
                      <td>{ticket.coordinates.y}</td>
                      <td>{ticket.person.passportId || '—'}</td>
                      <td title={ticket.event.name}>{truncate(ticket.event.name, 28)}</td>
                      <td>{formatMoney(ticket.price)}</td>
                      <td>
                        <TypeBadge type={ticket.type} />
                      </td>
                      <td>{ticket.discount ? `${ticket.discount}%` : '—'}</td>
                      <td>{ticket.number ?? '—'}</td>
                      <td title={ticket.venue.name}>{truncate(ticket.venue.name, 28)}</td>
                      <td className="row-actions">
                        <button title="Просмотреть" onClick={() => setDetail(ticket)}>
                          ○
                        </button>
                        <button
                          title="Изменить"
                          onClick={() => setDialog({ mode: 'edit', ticket })}
                        >
                          ✎
                        </button>
                        <button
                          className="danger-icon"
                          title="Удалить"
                          onClick={() => setConfirmDelete(ticket)}
                        >
                          ×
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {!result.data.items.length && (
              <EmptyState
                title="Билеты не найдены"
                text="Измените фильтр или создайте первый билет."
              />
            )}
            <Pagination
              page={result.data.page}
              totalPages={result.data.totalPages}
              total={result.data.totalItems}
              size={query.size}
              onPage={(page) => setQuery({ ...query, page })}
              onSize={(size) => setQuery({ ...query, page: 0, size })}
            />
          </>
        )}
      </div>
      {dialog && (
        <TicketDialog
          {...dialog}
          onClose={() => setDialog(null)}
          onSaved={() => {
            setDialog(null)
            onChanged(dialog.mode === 'create' ? 'Билет создан' : 'Билет изменён')
          }}
        />
      )}
      {detail && (
        <TicketDetails
          ticket={detail}
          onClose={() => setDetail(null)}
          onEdit={() => {
            setDialog({ mode: 'edit', ticket: detail })
            setDetail(null)
          }}
        />
      )}
      {confirmDelete && (
        <Confirm
          title={`Удалить «${truncate(confirmDelete.name, 60)}»?`}
          text="Билет будет удалён без возможности восстановления."
          error={confirmDelete.error}
          onCancel={() => setConfirmDelete(null)}
          onConfirm={remove}
        />
      )}
    </section>
  )
}
