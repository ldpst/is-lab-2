import { useState } from 'react'
import { api } from '../api'
import { useAsync } from '../hooks/useAsync'
import { ErrorState, Field, Loader, Modal } from './Common'

const EMPTY_TICKET = {
  name: '',
  x: '',
  y: '',
  personId: '',
  eventId: '',
  price: '',
  type: 'USUAL',
  discount: '',
  number: '',
  venueId: '',
}

export function TicketDialog({ mode, ticket, onClose, onSaved }) {
  const refs = useAsync(
    () =>
      Promise.all([
        api.get('/references/persons'),
        api.get('/references/events'),
        api.get('/references/venues'),
        api.get('/metadata'),
      ]),
    [],
  )
  const [form, setForm] = useState(
    ticket
      ? {
          name: ticket.name,
          x: ticket.coordinates.x,
          y: ticket.coordinates.y,
          personId: ticket.person.id,
          eventId: ticket.event.id,
          price: ticket.price,
          type: ticket.type,
          discount: ticket.discount ?? '',
          number: ticket.number ?? '',
          venueId: ticket.venue.id,
        }
      : EMPTY_TICKET,
  )
  const [errors, setErrors] = useState({})
  const [saving, setSaving] = useState(false)

  const change = (name, value) => {
    setForm({ ...form, [name]: value })
    setErrors({ ...errors, [name]: '' })
  }

  const submit = async (event) => {
    event.preventDefault()
    const validation = validateTicket(form)
    if (Object.keys(validation).length) {
      setErrors(validation)
      return
    }
    setSaving(true)
    const payload = {
      name: form.name.trim(),
      coordinates: { x: Number(form.x), y: Number(form.y) },
      personId: Number(form.personId),
      eventId: Number(form.eventId),
      price: Number(form.price),
      type: form.type,
      discount: form.discount === '' ? null : Number(form.discount),
      number: form.number === '' ? null : Number(form.number),
      venueId: Number(form.venueId),
      version: mode === 'create' ? null : ticket.version,
    }
    try {
      if (mode === 'create') await api.post('/tickets', payload)
      else await api.put(`/tickets/${ticket.id}`, payload)
      onSaved()
    } catch (error) {
      setErrors({ submit: error.message })
      setSaving(false)
    }
  }

  return (
    <Modal
      title={mode === 'create' ? 'Новый билет' : `Изменить билет #${ticket.id}`}
      subtitle="Поля со звёздочкой обязательны"
      onClose={onClose}
      wide
    >
      {refs.loading ? (
        <Loader />
      ) : refs.error ? (
        <ErrorState message={refs.error} />
      ) : (
        <form onSubmit={submit} className="form-grid">
          <Field label="Название *" error={errors.name}>
            <input
              value={form.name}
              onChange={(event) => change('name', event.target.value)}
              autoFocus
            />
          </Field>
          <Field label="Тип *" error={errors.type}>
            <select value={form.type} onChange={(event) => change('type', event.target.value)}>
              {refs.data[3].ticketTypes.map((value) => (
                <option key={value}>{value}</option>
              ))}
            </select>
          </Field>
          <Field label="Координата X *" hint="целое число" error={errors.x}>
            <input
              type="number"
              step="1"
              value={form.x}
              onChange={(event) => change('x', event.target.value)}
            />
          </Field>
          <Field label="Координата Y *" hint="целое число" error={errors.y}>
            <input
              type="number"
              step="1"
              value={form.y}
              onChange={(event) => change('y', event.target.value)}
            />
          </Field>
          <Field label="Цена *" hint="> 0" error={errors.price}>
            <input
              type="number"
              min="0.01"
              step="any"
              value={form.price}
              onChange={(event) => change('price', event.target.value)}
            />
          </Field>
          <Field label="Скидка" hint="> 0 и ≤ 100" error={errors.discount}>
            <input
              type="number"
              min="0.01"
              max="100"
              step="any"
              value={form.discount}
              onChange={(event) => change('discount', event.target.value)}
            />
          </Field>
          <Field label="Номер" hint="целое число > 0" error={errors.number}>
            <input
              type="number"
              min="1"
              step="1"
              value={form.number}
              onChange={(event) => change('number', event.target.value)}
            />
          </Field>
          <Field label="Владелец *" error={errors.personId}>
            <select
              value={form.personId}
              onChange={(event) => change('personId', event.target.value)}
            >
              <option value="">Выберите человека</option>
              {refs.data[0].map((value) => (
                <option key={value.id} value={value.id}>
                  {value.passportId} · {value.location.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Событие *" error={errors.eventId}>
            <select
              value={form.eventId}
              onChange={(event) => change('eventId', event.target.value)}
            >
              <option value="">Выберите событие</option>
              {refs.data[1].map((value) => (
                <option key={value.id} value={value.id}>
                  {value.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Площадка *" error={errors.venueId}>
            <select
              value={form.venueId}
              onChange={(event) => change('venueId', event.target.value)}
            >
              <option value="">Выберите площадку</option>
              {refs.data[2].map((value) => (
                <option key={value.id} value={value.id}>
                  {value.name} · {value.capacity} мест
                </option>
              ))}
            </select>
          </Field>
          {errors.submit && <div className="form-error full">{errors.submit}</div>}
          {(!refs.data[0].length || !refs.data[1].length || !refs.data[2].length) && (
            <div className="form-warning full">
              Перед созданием билета добавьте человека, событие и площадку в разделе «Справочники».
            </div>
          )}
          <div className="modal-actions full">
            <button type="button" className="button ghost" onClick={onClose}>
              Отмена
            </button>
            <button className="button primary" disabled={saving}>
              {saving ? 'Сохранение…' : 'Сохранить'}
            </button>
          </div>
        </form>
      )}
    </Modal>
  )
}

function validateTicket(form) {
  const errors = {}
  const name = form.name.trim()
  if (!name) errors.name = 'Введите название'
  if (form.x === '' || !Number.isInteger(Number(form.x))) errors.x = 'Введите целое значение X'
  if (form.y === '' || !Number.isInteger(Number(form.y))) errors.y = 'Введите целое значение Y'
  if (!(Number(form.price) > 0)) errors.price = 'Цена должна быть больше 0'
  if (form.discount !== '' && (!(Number(form.discount) > 0) || Number(form.discount) > 100))
    errors.discount = 'Скидка должна быть больше 0 и не больше 100'
  if (form.number !== '' && (!Number.isInteger(Number(form.number)) || !(Number(form.number) > 0)))
    errors.number = 'Номер должен быть целым и больше 0'
  for (const field of ['personId', 'eventId', 'venueId'])
    if (!form[field]) errors[field] = 'Выберите значение'
  return errors
}
