export async function handleResult(resp) {

  // if (resp.status === 401) {
  //   window.location.href = '/app/login'
  // }
  if (resp.status !== 200) {
    let reason = await resp.text()
    if (/json/i.test(resp.headers.get('Content-Type'))) {
      const data = JSON.parse(reason)
      reason = data.error || reason
    }
    if (!reason)
      reason = resp.statusText
    throw reason
  }
  return await resp.json()
}

export async function sendReq(method, url, data, token) {
  let headers = {
    'Content-Type': 'application/json',
    // 'Content-Type': 'multipart/form-data',
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const resp = await fetch(url, {
    method,
    credentials: 'same-origin',
    body: JSON.stringify(data),
    headers: new Headers(headers),
  })

  return await handleResult(resp)
}

export class BackendApi {
  constructor(token) {
    this.token = token
  }

  async authUser(email, password) {
    await sendReq('POST', '/auth/login', { email, password }, this.token)
  }

  async delete(url, data, token) {
    return await sendReq('DELETE', url, data, this.token)
  }

  async post(url, data) {
    return await sendReq('POST', url, data, this.token)
  }

  async get(url, token) {
    return await sendReq('GET', url, undefined, this.token)
  }

  async create(url, data, token) {
    return await sendReq('PUT', url, data, this.token)
  }

  async edit(url, data, token) {
    return await sendReq('PATCH', url, data, this.token)
  }

  async query(url, data) {
    return await sendReq('POST', url, data, this.token)
  }

  async upload(url, formData, method = 'POST') {
    const resp = await fetch(url, {
      method,
      credentials: 'same-origin',
      body: formData,
    })

    return await handleResult(resp)
  }
}

const backend = new BackendApi()
export default backend
