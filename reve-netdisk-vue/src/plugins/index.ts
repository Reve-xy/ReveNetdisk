import modal from './modal'
import {App} from "vue";

export default {
  install(app: App<Element>, options: any){
      app.provide('$modal',modal)
  }
}