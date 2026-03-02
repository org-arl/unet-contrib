<script setup>
import { useTemplateRef } from 'vue'
import { useStorage } from '@vueuse/core'
import Map from './Map.vue'

import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
const paneSplit = useStorage('usbl-pane-split', 50)

const host = import.meta.env.FJAGE_HOST || window.location.hostname
const port = import.meta.env.FJAGE_PORT || window.location.port
const shellUrl = `http://${host}:${port}/shell.html`
const modemIframe = useTemplateRef('modemIframe')

function close() {
  window.location.href = modemIframe.value.contentWindow.location.href
}
</script>

<template>
  <splitpanes @resized="paneSplit = $event.prevPane.size">
    <pane :size="paneSplit" min-size="30">
      <iframe ref="modemIframe" :src="shellUrl" style="width: 100%; height: 100vh; border: none" />
    </pane>
    <pane :size="100 - paneSplit" min-size="10">
      <div class="h-screen flex flex-col">
        <!-- Main -->
        <div class="flex-1 overflow-auto p-6">
          <Map />
        </div>
      </div>
      <!-- Close button -->
      <div class="absolute top-2 right-2">
        <button class="cursor-pointer" @click="close()">⊗</button>
      </div>
    </pane>
  </splitpanes>
</template>

<style>
@reference "./style.css";

.splitpanes--vertical>.splitpanes__splitter {
  @apply w-[2px] bg-gray-300 hover:bg-blue-700 hover:delay-1000;
}
</style>
