const{_INTERNAL_SOURCE_KEY:e}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:o}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{service:t}=window.moduleBroker.lookup("@ember/service"),{ajax:i}=window.moduleBroker.lookup("discourse/lib/ajax"),{popupAjaxError:r}=window.moduleBroker.lookup("discourse/lib/ajax-error"),{defaultHomepage:s}=window.moduleBroker.lookup("discourse/lib/utilities"),{default:n}=window.moduleBroker.lookup("discourse/routes/discourse")
var a={resource:"admin.adminPlugins.show",path:"/plugins",map(){this.route("discourse-chat-integration-providers",{path:"providers"},function(){this.route("show",{path:"/:provider"})})}},u=Object.freeze({__proto__:null,default:a})
const c=Object.freeze({type:"plugin",name:"discourse-chat-integration"})
const d="discourse-chat-integration"
var p={name:"chat-integration-admin-plugin-configuration-nav",initialize(t){const i=t.lookup("service:current-user")
i?.admin&&function(...t){const i="string"==typeof t[0]?2:1
t[i]={...t[i],[e]:c},o(...t)}(e=>{e.setAdminPluginIcon(d,"plug"),e.addAdminPluginConfigurationNav(d,[{label:"chat_integration.nav.providers",route:"adminPlugins.show.discourse-chat-integration-providers",description:"chat_integration.nav.providers_description"}])})}},l=Object.freeze({__proto__:null,default:p})
var h=Object.freeze({__proto__:null,default:function(){this.route("transcript",{path:"/chat-transcript/:secret"})}})
class g extends n{static{dt7948.g(this.prototype,"currentUser",[t])}#e=void dt7948.i(this,"currentUser")
static{dt7948.g(this.prototype,"composer",[t])}#o=void dt7948.i(this,"composer")
static{dt7948.g(this.prototype,"router",[t])}#t=void dt7948.i(this,"router")
async model(e){if(this.currentUser){await this.router.replaceWith(`discovery.${s()}`).followRedirects()
try{const{content:o}=await i(`/chat-transcript/${e.secret}`)
this.composer.openNewTopic({body:o})}catch(e){r(e)}}else this.send("showLogin")}}const m={"discourse/admin-chat-integration-plugin-route-map":u,"discourse/initializers/chat-integration-admin-plugin-configuration-nav":l,"discourse/public-route-map":h,"discourse/routes/transcript":Object.freeze({__proto__:null,default:g})}
export{m as default}

//# sourceMappingURL=../../map/plugins/discourse-chat-integration_main.DgAUPk-1kt3p7gy.digested.js.map
