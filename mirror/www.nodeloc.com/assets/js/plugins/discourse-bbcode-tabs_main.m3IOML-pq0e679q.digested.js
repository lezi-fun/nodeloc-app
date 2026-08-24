const{_INTERNAL_SOURCE_KEY:t}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:e}=window.moduleBroker.lookup("discourse/lib/plugin-api")
let o=0
const s={tag:"tabs",replace:function(t,e,s){const i=o++
t.push("html_raw","",0).content='<div class="tabs"><div class="tabs-title">'
const n=/\[tab=([^\]]*)\]([\s\S]*?)(?=\[tab=|\[\/tab\]|\[\/tabs\]|$)/g
let c,a=1
const r=[]
for(;null!==(c=n.exec(s));)c[1]&&c[2]&&r.push({title:c[1].trim(),content:c[2].trim(),index:a++})
const d=t.md.options.discourse?.siteSettings,b=!1!==d?.bbcode_tabs_first_tab_active
for(const e of r){const o=`tab-${i}-${e.index}`,s=b&&1===e.index?' checked="checked"':"",n=e.title.replace(/["']/g,"").replace(/&quot;|&apos;|&#39;/g,"")
t.push("html_raw","",0).content=`<div class="tab"><input type="radio" name="tab-group-${i}" ${s} id="${o}"><label for="${o}">${n}</label><div class="content">`
const c={},a=[]
t.md.block.parse(e.content,t.md,c,a)
for(let e=0;e<a.length;e++)t.tokens.push(a[e])
t.push("html_raw","",0).content="</div></div>"}return t.push("html_raw","",0).content="</div></div>",!0}}
function i(t){t.allowList(["div.tabs","div.tabs-title","div.tab","div.content","input[type=radio]","input[name=tab-group-*]","input[checked=checked]","input[id=tab-*]","label[for=tab-*]"]),t.registerOptions((t,e)=>{t.discourse=t.discourse||{},t.discourse.siteSettings=e,t.features["discourse-bbcode-tabs"]=!0}),t.registerPlugin(t=>{o=0,t.block.bbcode.ruler.push("tabs",s)})}var n=Object.freeze({__proto__:null,setup:i}),c=Object.freeze({__proto__:null,default:i})
const a=Object.freeze({type:"plugin",name:"discourse-bbcode-tabs"})
var r={name:"discourse-bbcode-tabs",initialize(){(function(...o){const s="string"==typeof o[0]?2:1
o[s]={...o[s],[t]:a},e(...o)})("0.8.7",t=>{t.decorateCooked(e=>{const o=e.find(".tabs")
0!==o.length&&o.each((e,o)=>{const s=$(o)
s.find(".tab label").attr("role","tab")
s.find(".tab .content").attr("role","tabpanel"),s.find(".tab input").on("keydown",function(t){if("ArrowLeft"===t.key||"ArrowRight"===t.key){t.preventDefault()
const e=s.find(".tab input"),o=e.index(this)
let i
i="ArrowLeft"===t.key?(o-1+e.length)%e.length:(o+1)%e.length,e.eq(i).prop("checked",!0).focus()}})
if(t.siteSettings&&!1!==t.siteSettings.bbcode_tabs_remember_selection){const t=s.find(".tab input").first().attr("name")
if(t){const e=localStorage.getItem(`discourse-tabs-${t}`)
if(e){const t=s.find(`#${e}`)
t.length&&t.prop("checked",!0)}s.find(".tab input").on("change",function(){this.checked&&localStorage.setItem(`discourse-tabs-${t}`,this.id)})}}})},{id:"discourse-bbcode-tabs"})})}}
const d={"discourse-markdown/discourse-bbcode-tabs":c,"initializers/discourse-bbcode-tabs":Object.freeze({__proto__:null,default:r}),"lib/discourse-markdown/bbcode-tabs":n}
export{d as default}

//# sourceMappingURL=../../map/plugins/discourse-bbcode-tabs_main.m3IOML-pq0e679q.digested.js.map
