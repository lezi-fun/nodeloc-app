const{getObjectForTheme:e}=window.moduleBroker.lookup("discourse/lib/theme-settings-store"),{action:t}=window.moduleBroker.lookup("@ember/object"),{setOwner:o}=window.moduleBroker.lookup("@ember/owner"),{_INTERNAL_SOURCE_KEY:i}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:s}=window.moduleBroker.lookup("discourse/lib/plugin-api"),n=e(21),r=Object.freeze({type:"theme",id:21})
class l{constructor(e,i){o(this,e)
const s=n.omitted_emoji.split("|").filter(Boolean),r=n.omitted_emoji_groups.split("|").filter(Boolean)
i.modifyClass("component:emoji-picker/content",e=>class extends e{get groups(){const e=super.groups
if(!r.length&&!s.length)return e
return Object.fromEntries(Object.entries(e).map(([e,t])=>r.length&&r.includes(e)?null:(s.length&&(t=t.filter(e=>!s.includes(e.name))),[e,t])).filter(Boolean))}get flatEmojis(){const e=super.flatEmojis
return s.length?e.filter(e=>!s.includes(e.name)):e}}),i.modifyClass("component:d-editor",e=>class extends e{setupEditor(e){const t=super.setupEditor(e),o=document.querySelector(".d-editor-textarea-wrapper")
if(!o||!s.length)return t
const i=new MutationObserver(e=>{e.forEach(e=>{const t=e.addedNodes[0]
t?.classList?.contains("autocomplete")&&t?.classList?.contains("ac-emoji")&&s.forEach(e=>{const o=t.querySelector(`img.emoji[src*="${e}.png"]`)
o&&(o.parentNode.parentNode.style.display="none")})})})
return i.observe(o,{childList:!0}),()=>{t?.(),i?.disconnect()}}static{dt7948.n(this.prototype,"setupEditor",[t])}})}}var c={name:"discourse-omit-emoji",initialize(e){(function(...e){const t="string"==typeof e[0]?2:1
e[t]={...e[t],[i]:r},s(...e)})("0.35.0",t=>{this.instance=new l(e,t)})},tearDown(){this.instance=null}}
const u={"discourse/api-initializers/discourse-omit-emoji":Object.freeze({__proto__:null,default:c})}
export{u as default}

//# sourceMappingURL=5baa936fc9a2c433ae8e040653bd65ea4bf78fc8.map?__ws=www.nodeloc.com
