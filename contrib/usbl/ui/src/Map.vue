<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { Gateway, MessageClass } from 'fjage'
import { BoundedSortedMap } from './BoundedSortedMap.js'

import "leaflet/dist/leaflet.css"
import { CRS } from "leaflet"
import { LMap, LControlScale, LCircleMarker, LCircle, LPolygon } from "@vue-leaflet/vue-leaflet"

const RangeNtf = MessageClass('org.arl.unet.localization.RangeNtf')
const BearingNtf = MessageClass('org.arl.unet.localization.BearingNtf')
const PeerLocationNtf = MessageClass('org.arl.unet.localization.PeerLocationNtf')

const gw = new Gateway({
  hostname: import.meta.env.FJAGE_HOST || window.location.hostname,
  port: import.meta.env.FJAGE_PORT || window.location.port,
  returnNullOnFailedResponse: false,
})
gw.subscribe(gw.agent('ranging'))
gw.subscribe(gw.topic('bearings'))
gw.subscribe(gw.topic('locations'))

const arrivals = reactive(new BoundedSortedMap(100))
gw.addMessageListener(msg => {
  try {
    if (msg instanceof RangeNtf) {
      const arrival = arrivals.getOrInsert(msg.rxStartTime, {})
      if (arrival === undefined) { return }
      arrival.peer = msg.to
      arrival.range = msg.range
    }
    if (msg instanceof BearingNtf) {
      const arrival = arrivals.getOrInsert(msg.rxStartTime, {})
      if (arrival === undefined) { return }
      arrival.azimuth = msg.azimuth
      arrival.elevation = msg.elevation
    }
    if (msg instanceof PeerLocationNtf) {
      const arrival = arrivals.getOrInsert(msg.rxStartTime, {})
      if (arrival === undefined) { return }
      arrival.peer = msg.peer
      arrival.location = msg.peerLocation
    }
  }
  catch (e) {
    console.error('Error processing PeerLocationNtf:', e)
  }
})

const selectedArrivals = computed(() => {
  function* select() {
    let i = 0
    for (const arrival of arrivals.values()) {
      if (i >= count.value) { break }
      if (arrival[type.value] !== undefined) {
        yield arrival
        i += 1
      }
    }
  }
  return Array.from(select())
})

const type = ref('location')
const count = ref(1)
watch(count, (newCount) => {
  count.value = Math.min(Math.max(Math.round(newCount), 1), 99)
})
const showLocation = ref(true)
const showRange = ref(true)
const showAzimuth = ref(true)
const correctRangeByElevation = ref(true)
</script>

<template>
  <div class="h-full flex flex-col justify-stretch">
    <h1 class="text-xl font-bold mb-2">USBL</h1>
    <div class="flex-none flex flex-row items-center gap-2 flex-wrap mb-4">
      <label class="input w-30">
        <span class="label">Last</span>
        <input v-model="count" type="number" class="text-right" />
      </label>
      <label class="select w-35">
        <select v-model="type">
          <option value="location">Locations</option>
          <option value="range">Ranges</option>
          <option value="azimuth">Azimuths</option>
        </select>
      </label>
      <div class="flex flex-row items-center gap-2 h-10 px-3 border border-[#18181b33] rounded-sm">
        Show:
        <label class="label">
          <input type="checkbox" v-model="showLocation" class="checkbox" />
          Location
        </label>
        <label class="label">
          <input type="checkbox" v-model="showRange" class="checkbox" />
          Range
        </label>
        <label class="label">
          <input type="checkbox" v-model="showAzimuth" class="checkbox" />
          Azimuth
        </label>
      </div>
      <div class="flex items-center h-10 px-3 border border-[#18181b33] rounded-sm">
        <label class="label">
          <input type="checkbox" v-model="correctRangeByElevation" class="checkbox" />
          Apply elevation to range
        </label>
      </div>
    </div>
    <div class="flex-1 w-full">
      <LMap :zoom="0" :center="[0, 0]" :min-zoom="-1" :crs="CRS.Simple" :use-global-leaflet="false">
        <LControlScale position="topright" :imperial="false" :metric="true" :useGlobalLeaflet="false" />
        <LCircleMarker color="#000" :radius="5" :lat-lng="[0, 0]" />
        <template v-for="arrival in selectedArrivals" :key="arrival">
          <LCircleMarker v-if="showLocation && arrival.location !== undefined" :radius="5"
            :lat-lng="[arrival.location[1], arrival.location[0]]" />
          <LCircle v-if="showRange && !correctRangeByElevation && arrival.range !== undefined" :lat-lng="[0, 0]"
            :radius="arrival.range" color="#000" :fill="false" :weight="1" />
          <LCircle
            v-if="showRange && correctRangeByElevation && arrival.range !== undefined && arrival.elevation !== undefined"
            :lat-lng="[0, 0]" :radius="arrival.range * Math.cos(arrival.elevation * Math.PI / 180)" color="#000"
            :fill="false" :weight="1" />
          <LPolygon v-if="showAzimuth && arrival.azimuth !== undefined"
            :lat-lngs="[[0, 0], [1000 * Math.sin(arrival.azimuth * Math.PI / 180), 1000 * Math.cos(arrival.azimuth * Math.PI / 180)]]"
            color="#000" :fill="false" :weight="1" />
        </template>
      </LMap>
    </div>
  </div>
</template>
