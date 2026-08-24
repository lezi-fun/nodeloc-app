const{default:e}=window.moduleBroker.lookup("@glimmer/component"),{tracked:t,cached:s}=window.moduleBroker.lookup("@glimmer/tracking"),{default:n,service:r}=window.moduleBroker.lookup("@ember/service"),{default:i}=window.moduleBroker.lookup("discourse/helpers/helper-fn"),{default:o}=window.moduleBroker.lookup("discourse/models/composer"),{gt:l}=window.moduleBroker.lookup("discourse/truth-helpers"),{default:a}=window.moduleBroker.lookup("discourse/ui-kit/d-user-link"),{default:u}=window.moduleBroker.lookup("discourse/ui-kit/helpers/d-avatar"),{i18n:c}=window.moduleBroker.lookup("discourse-i18n"),{setComponentTemplate:p}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:d}=window.moduleBroker.lookup("@ember/template-factory"),{default:h}=window.moduleBroker.lookup("@ember/component/template-only"),{concat:m}=window.moduleBroker.lookup("@ember/helper"),{trustHTML:g}=window.moduleBroker.lookup("@ember/template"),{translateSize:w}=window.moduleBroker.lookup("discourse/lib/avatar-utils"),{_INTERNAL_SOURCE_KEY:y}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:k}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{debounce:b,cancel:C}=window.moduleBroker.lookup("@ember/runloop"),{isTesting:f}=window.moduleBroker.lookup("discourse/lib/environment")
class v extends e{static{dt7948.g(this.prototype,"presence",[r])}#e=void dt7948.i(this,"presence")
static{dt7948.g(this.prototype,"composerPresenceManager",[r])}#t=void dt7948.i(this,"composerPresenceManager")
static{dt7948.g(this.prototype,"currentUser",[r])}#s=void dt7948.i(this,"currentUser")
static{dt7948.g(this.prototype,"siteSettings",[r])}#n=void dt7948.i(this,"siteSettings")
static{dt7948.g(this.prototype,"replyChannel",[t])}#r=void dt7948.i(this,"replyChannel")
static{dt7948.g(this.prototype,"whisperChannel",[t])}#i=void dt7948.i(this,"whisperChannel")
static{dt7948.g(this.prototype,"editChannel",[t])}#o=void dt7948.i(this,"editChannel")
static{dt7948.g(this.prototype,"translateChannel",[t])}#l=void dt7948.i(this,"translateChannel")
setupReplyChannel=i((e,t)=>{const{topic:s}=this.args.model
if(!s||!this.isReply)return
const n=`/discourse-presence/reply/${s.id}`,r=this.presence.getChannel(n)
this.replyChannel=r,r.subscribe(),t.cleanup(()=>r.unsubscribe())})
setupWhisperChannel=i((e,t)=>{const{topic:s}=this.args.model,{whisperer:n}=this.currentUser
if(!s||!this.isReply||!n)return
const r=`/discourse-presence/whisper/${s.id}`,i=this.presence.getChannel(r)
this.whisperChannel=i,i.subscribe(),t.cleanup(()=>i.unsubscribe())})
setupEditChannel=i((e,t)=>{const{post:s}=this.args.model
if(!s||!this.isEdit)return
const n=`/discourse-presence/edit/${s.id}`,r=this.presence.getChannel(n)
this.editChannel=r,r.subscribe(),t.cleanup(()=>r.unsubscribe())})
setupTranslateChannel=i((e,t)=>{const{post:s}=this.args.model
if(!s||!this.isTranslate)return
const n=`/discourse-presence/translate/${s.id}`,r=this.presence.getChannel(n)
this.translateChannel=r,r.subscribe(),t.cleanup(()=>r.unsubscribe())})
notifyState=i((e,t)=>{const{topic:s,post:n,replyDirty:r}=this.args.model,i=this.isEdit||this.isTranslate?n:s
if(i){const e=`/discourse-presence/${this.state}/${i.id}`
this.composerPresenceManager.notifyState(e,r)}t.cleanup(()=>this.composerPresenceManager.leave())})
get isReply(){return"reply"===this.state||"whisper"===this.state}get isEdit(){return"edit"===this.state}get isTranslate(){return"translate"===this.state}get state(){const{editingPost:e,whisper:t,replyingToTopic:s,action:n}=this.args.model
return n===o.ADD_TRANSLATION?"translate":e?"edit":t?"whisper":s?"reply":void 0}static{dt7948.n(this.prototype,"state",[s])}get users(){let e
if(this.isEdit)e=this.editChannel?.users||[]
else if(this.isTranslate)e=this.translateChannel?.users||[]
else{e=[...this.replyChannel?.users||[],...this.whisperChannel?.users||[]]}return e.filter(e=>e.id!==this.currentUser.id).slice(0,this.siteSettings.presence_max_users_shown)}static{dt7948.n(this.prototype,"users",[s])}static{p(d({id:null,block:'[[[41,[30,0,["currentUser"]],[[[1,"  "],[1,[30,0,["setupReplyChannel"]]],[1,"\\n  "],[1,[30,0,["setupWhisperChannel"]]],[1,"\\n  "],[1,[30,0,["setupEditChannel"]]],[1,"\\n  "],[1,[30,0,["setupTranslateChannel"]]],[1,"\\n  "],[1,[30,0,["notifyState"]]],[1,"\\n\\n"],[41,[28,[32,0],[[30,0,["users","length"]],0],null],[[[1,"    "],[10,0],[14,0,"presence-users"],[12],[1,"\\n      "],[10,0],[14,0,"presence-avatars"],[12],[1,"\\n"],[42,[28,[31,2],[[28,[31,2],[[30,0,["users"]]],null]],null],null,[[[1,"          "],[8,[32,1],null,[["@user"],[[30,1]]],[["default"],[[[[1,"\\n            "],[1,[28,[32,2],[[30,1]],[["imageSize"],["small"]]]],[1,"\\n          "]],[]]]]],[1,"\\n"]],[1]],null],[1,"      "],[13],[1,"\\n\\n      "],[10,1],[14,0,"presence-text"],[12],[1,"\\n        "],[10,1],[14,0,"description"],[12],[41,[30,0,["isReply"]],[[[1,[28,[32,3],["presence.replying"],[["count"],[[30,0,["users","length"]]]]]]],[]],[[[41,[30,0,["isTranslate"]],[[[1,[28,[32,3],["presence.translating"],[["count"],[[30,0,["users","length"]]]]]]],[]],[[[1,[28,[32,3],["presence.editing"],[["count"],[[30,0,["users","length"]]]]]]],[]]]],[]]],[13],[1,"\\n        "],[10,1],[14,0,"wave"],[12],[1,"\\n          "],[10,1],[14,0,"dot"],[12],[1,"."],[13],[1,"\\n          "],[10,1],[14,0,"dot"],[12],[1,"."],[13],[1,"\\n          "],[10,1],[14,0,"dot"],[12],[1,"."],[13],[1,"\\n        "],[13],[1,"\\n      "],[13],[1,"\\n    "],[13],[1,"\\n"]],[]],null]],[]],null]],["user"],["if","each","-track-array"]]',moduleName:"(unknown template module)",scope:()=>[l,a,u,c],isStrictMode:!0}),this)}}var _=Object.freeze({__proto__:null,default:v})
class T extends e{static{dt7948.g(this.prototype,"presence",[r])}#e=void dt7948.i(this,"presence")
static{dt7948.g(this.prototype,"currentUser",[r])}#s=void dt7948.i(this,"currentUser")
static{dt7948.g(this.prototype,"replyChannel",[t])}#r=void dt7948.i(this,"replyChannel")
static{dt7948.g(this.prototype,"whisperChannel",[t])}#i=void dt7948.i(this,"whisperChannel")
setupReplyChannel=i((e,t)=>{const{topic:s}=this.args
if(!s)return
const n=`/discourse-presence/reply/${s.id}`,r=this.presence.getChannel(n)
this.replyChannel=r,r.subscribe(),t.cleanup(()=>r.unsubscribe())})
setupWhisperChannel=i((e,t)=>{const{topic:s}=this.args,{whisperer:n}=this.currentUser
if(!s||!n)return
const r=`/discourse-presence/whisper/${s.id}`,i=this.presence.getChannel(r)
this.whisperChannel=i,i.subscribe(),t.cleanup(()=>i.unsubscribe())})
get users(){return[...this.replyChannel?.users||[],...this.whisperChannel?.users||[]].filter(e=>e.id!==this.currentUser.id)}static{dt7948.n(this.prototype,"users",[s])}static{p(d({id:null,block:'[[[41,[30,0,["currentUser"]],[[[1,"  "],[1,[30,0,["setupReplyChannel"]]],[1,"\\n  "],[1,[30,0,["setupWhisperChannel"]]],[1,"\\n\\n"],[41,[28,[32,0],[[30,0,["users","length"]],0],null],[[[1,"    "],[10,0],[14,0,"presence-users"],[12],[1,"\\n      "],[10,0],[14,0,"presence-avatars"],[12],[1,"\\n"],[42,[28,[31,2],[[28,[31,2],[[30,0,["users"]]],null]],null],null,[[[1,"          "],[8,[32,1],null,[["@user"],[[30,1]]],[["default"],[[[[1,"\\n            "],[1,[28,[32,2],[[30,1]],[["imageSize"],[[30,2]]]]],[1,"\\n          "]],[]]]]],[1,"\\n"]],[1]],null],[1,"      "],[13],[1,"\\n\\n      "],[10,1],[14,0,"presence-text"],[12],[1,"\\n        "],[10,1],[14,0,"description"],[12],[1,"\\n          "],[1,[28,[32,3],["presence.replying_to_topic"],[["count"],[[30,0,["users","length"]]]]]],[1,"\\n        "],[13],[1,"\\n        "],[10,1],[14,0,"wave"],[12],[1,"\\n          "],[10,1],[14,0,"dot"],[12],[1,"."],[13],[1,"\\n          "],[10,1],[14,0,"dot"],[12],[1,"."],[13],[1,"\\n          "],[10,1],[14,0,"dot"],[12],[1,"."],[13],[1,"\\n        "],[13],[1,"\\n      "],[13],[1,"\\n    "],[13],[1,"\\n"]],[]],null]],[]],null]],["user","@avatarSize"],["if","each","-track-array"]]',moduleName:"(unknown template module)",scope:()=>[l,a,u,c],isStrictMode:!0}),this)}}var B=Object.freeze({__proto__:null,default:T})
const S=p(d({id:null,block:'[[[10,0],[14,0,"before-composer-controls-outlet presence"],[12],[1,"\\n  "],[8,[32,0],null,[["@model"],[[30,1,["model"]]]],null],[1,"\\n"],[13]],["@outletArgs"],[]]',moduleName:"(unknown template module)",scope:()=>[v],isStrictMode:!0}),h())
var U=Object.freeze({__proto__:null,default:S})
const z="small"
class R extends e{get avatarDimensions(){return w(z)}static{p(d({id:null,block:'[[[10,0],[15,5,[28,[32,0],[[28,[32,1],["--avatar-min-height: ",[30,0,["avatarDimensions"]],"px"],null]],null]],[14,0,"topic-above-footer-buttons-outlet presence"],[12],[1,"\\n  "],[8,[32,2],null,[["@topic","@avatarSize"],[[30,1,["model"]],[32,3]]],null],[1,"\\n"],[13]],["@outletArgs"],[]]',moduleName:"(unknown template module)",scope:()=>[g,m,T,z],isStrictMode:!0}),this)}}var M=Object.freeze({__proto__:null,default:R})
const x=Object.freeze({type:"plugin",name:"discourse-presence"})
var E={name:"presence-admin-plugin-configuration-nav",initialize(e){const t=e.lookup("service:current-user")
t?.admin&&function(...e){const t="string"==typeof e[0]?2:1
e[t]={...e[t],[y]:x},k(...e)}(e=>{e.setAdminPluginIcon("discourse-presence","eye")})}},O=Object.freeze({__proto__:null,default:E})
class A extends n{static{dt7948.g(this.prototype,"currentUser",[r])}#s=void dt7948.i(this,"currentUser")
static{dt7948.g(this.prototype,"presence",[r])}#e=void dt7948.i(this,"presence")
notifyState(e,t=!0,s=1e4){t?this.currentUser.user_option.hide_presence||this._name!==e&&(this.leave(),this._name=e,this._channel=this.presence.getChannel(e),this._channel.enter(),f()||(this._autoLeaveTimer=b(this,this.leave,s))):this.leave()}leave(){this._autoLeaveTimer&&(C(this._autoLeaveTimer),this._autoLeaveTimer=null),this._channel?.leave(),this._channel=null,this._name=null}}const N={"discourse/components/composer-presence-display":_,"discourse/components/topic-presence-display":B,"discourse/connectors/before-composer-controls/presence":U,"discourse/connectors/topic-above-footer-buttons/presence":M,"discourse/initializers/presence-admin-plugin-configuration-nav":O,"discourse/services/composer-presence-manager":Object.freeze({__proto__:null,default:A})}
export{N as default}

//# sourceMappingURL=../../map/plugins/discourse-presence_main.Dl8_5h-osi4j1us.digested.js.map
