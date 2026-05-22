export function downloadBlob(response, fallbackName) {
  const disposition = response.headers['content-disposition'] || ''
  const match = disposition.match(/filename\*=UTF-8''([^;]+)/)
  const filename = match ? decodeURIComponent(match[1]) : fallbackName
  const blob = new Blob([response.data])
  const link = document.createElement('a')
  link.href = window.URL.createObjectURL(blob)
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(link.href)
}
