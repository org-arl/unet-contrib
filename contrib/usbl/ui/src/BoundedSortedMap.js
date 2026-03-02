export class BoundedSortedMap {
  constructor(max) {
    this.count = 0
    this.max = max
    this.entries = new Array(max)
  }

  getOrInsert(key, value) {
    let i = 0
    while (i < this.count && this.entries[i].key > key) {
      i += 1
    }
    if (i >= this.max) {
      return undefined
    }
    if (this.entries[i] === undefined) {
      this.entries[i] = { key: key, value: value }
      this.count += 1
      return value
    }
    if (this.entries[i].key === key) {
      return this.entries[i].value
    }
    let tmp = this.entries[i]
    this.entries[i] = { key: key, value: value }
    i += 1
    while (i < this.count) {
      const next = this.entries[i]
      this.entries[i] = tmp
      tmp = next
      i += 1
    }
    if (i < this.max) {
      this.entries[i] = tmp
      this.count += 1
    }
    return value
  }

  *values() {
    for (let i = 0; i < this.count; i++) {
      yield this.entries[i].value
    }
  }
}
