import{w as e,d as s,M as i,e as o,f as d,g as u}from"./discourse-custom-badge_chunk.vOVb-p-kfcdr609.digested.js"
const{default:a}=window.moduleBroker.lookup("@ember/service")
var t={resource:"admin.adminPlugins.show",path:"/plugins",map(){this.route("discourse-custom-badge"),this.route("discourse-custom-badge-groups",{path:"discourse-custom-badge/groups"})}},r=Object.freeze({__proto__:null,default:t})
var c={name:"discourse-custom-badge-admin-plugin-configuration-nav",initialize(s){const i=s.lookup("service:current-user")
i&&i.admin&&e(e=>{e.addAdminPluginConfigurationNav("discourse-custom-badge",[{label:"discourse_custom_badge.admin.nav.badge_styles",route:"adminPlugins.show.discourse-custom-badge",description:"discourse_custom_badge.admin.title_description"},{label:"discourse_custom_badge.admin.nav.group_styles",route:"adminPlugins.show.discourse-custom-badge-groups",description:"discourse_custom_badge.admin.group_title_description"}])})}},n=Object.freeze({__proto__:null,default:c})
const l={"discourse/admin-discourse-custom-badge-plugin-route-map":r,"discourse/initializers/discourse-custom-badge-admin-plugin-configuration-nav":n,"discourse/initializers/discourse-custom-badge":u,"discourse/lib/badge-style-helper":d,"discourse/lib/text-effect-definitions":o,"discourse/modifiers/apply-badge-style":i,"discourse/services/badge-style":Object.freeze({__proto__:null,default:class extends a{applyStyles(e,i){s(e,i)}}})}
export{l as default}

//# sourceMappingURL=../../map/plugins/discourse-custom-badge_main.CR4h0S-kfcdr609.digested.js.map
