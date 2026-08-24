const{ajax:e}=window.moduleBroker.lookup("discourse/lib/ajax"),{popupAjaxError:o}=window.moduleBroker.lookup("discourse/lib/ajax-error")
import{w as t}from"./automation_chunk.DvFpQj-ivdm7a4p.digested.js"
var i={resource:"admin.adminPlugins.show",path:"/plugins",map(){this.route("automation",function(){this.route("new"),this.route("edit",{path:"/:id"})})}},n=Object.freeze({__proto__:null,default:i})
function a(t){e(`/append-last-checked-by/${t.currentTarget.postId}`,{type:"PUT"}).catch(o)}function r(e){e.getCurrentUser()&&e.decorateCookedElement(u,{id:"discourse-automation"})}function u(e,o){if(!o)return
const t=e.querySelectorAll(".btn-checked"),i=o.getModel()
Array.from(t).forEach(e=>{e.postId=i.id,e.addEventListener("click",a,!1)})}var d={name:"append-by-listener",initialize(){t(r)}},s=Object.freeze({__proto__:null,default:d})
const l="automation"
var c={name:"automation-admin-plugin-configuration-nav",initialize(e){const o=e.lookup("service:current-user")
o?.admin&&t(e=>{e.setAdminPluginIcon(l,"wand-magic-sparkles"),e.addAdminPluginConfigurationNav(l,[{label:"discourse_automation.title",route:"adminPlugins.show.automation"}])})}}
const m={"admin-automation-route-map":n,"discourse/initializers/append-by-listener":s,"discourse/initializers/automation-admin-plugin-configuration-nav":Object.freeze({__proto__:null,default:c})}
export{m as default}

//# sourceMappingURL=../../map/plugins/automation_main.j-gi7s-ivdm7a4p.digested.js.map
