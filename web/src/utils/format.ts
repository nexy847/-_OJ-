import dayjs from 'dayjs'

export function formatDateTime(value?: string | null) {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
}

export function formatPercent(value: number) {
  return `${(value * 100).toFixed(1)}%`
}

export function formatNumber(value?: number | null, digits = 2) {
  if (value === undefined || value === null) {
    return '-'
  }
  return Number(value).toFixed(digits)
}
