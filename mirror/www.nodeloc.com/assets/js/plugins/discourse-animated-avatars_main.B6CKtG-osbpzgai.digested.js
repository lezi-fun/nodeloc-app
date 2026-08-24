const{trustHTML:e}=window.moduleBroker.lookup("@ember/template"),{default:a}=window.moduleBroker.lookup("discourse/ui-kit/helpers/d-bound-avatar"),{prefersReducedMotion:t}=window.moduleBroker.lookup("discourse/lib/utilities"),{setComponentTemplate:r}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:o}=window.moduleBroker.lookup("@ember/template-factory"),{default:n}=window.moduleBroker.lookup("@ember/component/template-only"),{next:s}=window.moduleBroker.lookup("@ember/runloop"),{_INTERNAL_SOURCE_KEY:l}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:i}=window.moduleBroker.lookup("discourse/lib/plugin-api")
function u(r,o){const n=a(r,o)
return!r.animated_avatar||t()?n:e(n.toString().replace(/\.png/,".gif"))}var d=Object.freeze({__proto__:null,default:u})
const c=r(o({id:null,block:'[[[1,[28,[32,0],[[30,1,["user"]],"huge"],null]]],["@outletArgs"],[]]',moduleName:"(unknown template module)",scope:()=>[u],isStrictMode:!0}),n())
var p=Object.freeze({__proto__:null,default:c})
const m=Object.freeze({type:"plugin",name:"discourse-animated-avatars"})
let v=[],f=!0
function w(e=!1){v?.forEach(e=>{e.src=e.src.replace(/\.gif$/,".png")}),e?f=!1:v=[]}var k={name:"animated-avatars",initialize(){(function(...e){const a="string"==typeof e[0]?2:1
e[a]={...e[a],[l]:m},i(...e)})(e=>{t()||(function(e){const a=Symbol("avatar-animated-state")
e.addTrackedPostProperties("animated_avatar"),e.addPostClassesCallback(e=>null!=e?.animated_avatar?["animated-avatar"]:[])
const t=e.container.lookup("service:site-settings")
e.registerValueTransformer("post-avatar-template",({value:e,context:{post:r,keyboardSelected:o,decoratorState:n}})=>e&&r.animated_avatar&&(t.animated_avatars_always_animate||o||n?.get(a))?e.replace(/\.png$/,".gif"):e),t.animated_avatars_always_animate||e.registerValueTransformer("post-event-listener",({value:e,context:{decoratorState:t}})=>{e.push({event:"mouseenter",callback:()=>{t.has(a)||t.set(a,!0)}}),e.push({event:"mouseleave",callback:()=>{t.delete(a)}})})}(e),window.addEventListener("blur",this.blurEvent),window.addEventListener("focus",this.focusEvent),e.customUserAvatarClasses(e=>null!=e?.animated_avatar?["animated-avatar"]:[]),e.onAppEvent("user-card:after-show",()=>{s(()=>{var e
w(),(e=document.querySelector("#user-card img.animated-avatar"))&&f&&e.src.replace(/\.png$/,".gif")!==e.src&&(e.src=e.src.replace(/\.png$/,".gif"),v.push(e))})}))})},blurEvent(){w(!0)},focusEvent(){f=!0,v?.forEach(e=>{e.src=e.src.replace(/\.png$/,".gif")})},teardown(){window.removeEventListener("blur",this.blurEvent),window.removeEventListener("focus",this.focusEvent)}}
const g={"app/helpers/animated-bound-avatar":d,"discourse/connectors/user-profile-avatar-img-wrapper/user-profile-avatar-img-wrapper":p,"initializers/animated-avatars":Object.freeze({__proto__:null,default:k})}
export{g as default}

//# sourceMappingURL=../../map/plugins/discourse-animated-avatars_main.B6CKtG-osbpzgai.digested.js.map
