const{getObjectForTheme:e}=window.moduleBroker.lookup("discourse/lib/theme-settings-store"),{default:t}=window.moduleBroker.lookup("discourse/lib/hashtag-types/tag"),{iconHTML:o}=window.moduleBroker.lookup("discourse/lib/icon-library"),{_INTERNAL_SOURCE_KEY:r}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:s}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{defaultRenderTag:i}=window.moduleBroker.lookup("discourse/lib/render-tag"),n=e(12),l=Object.freeze({type:"theme",id:12})
const a=e=>{const t=(e=>{if("string"!=typeof e)throw new TypeError("Hex color must be a string")
let t=e.trim().replace(/^#/,"")
if(3===t.length&&(t=t.split("").map(e=>e+e).join("")),!/^[0-9a-fA-F]{6}$/.test(t))return!1
const o=parseInt(t,16)
return[o>>16&255,o>>8&255,255&o]})(e)
return t?(e=>{const t=[e[0],e[1],e[2]].map(e=>(e/=255)<=.03928?e/12.92:Math.pow((e+.055)/1.055,2.4))
return.2126*t[0]+.7152*t[1]+.0722*t[2]})(t)>=.45?"#000d":"#fffd":""}
var c=Object.freeze({__proto__:null,contrastColor:a})
function u(e,t){const r=i(e,t),s="string"==typeof e?e:e.slug||e.name,l=n.tag_icon_list.split("|").find(e=>e.indexOf(",")>-1&&s.toLowerCase()===e.substr(0,e.indexOf(",")).toLowerCase())
if(l){const[,e,t]=l.split(","),s=new DOMParser,i=s.parseFromString(r,"text/html").body.firstChild,n=s.parseFromString(`<span class="tag-icon">${o(e)}</span>`,"text/html").body.firstChild
return i.prepend(n),i.classList.add("discourse-tag--tag-icons-style"),i.style.setProperty("--color1",t??""),i.style.setProperty("--color2",t?a(t):""),i.outerHTML}return r}class d extends t{constructor(e,t){super(t),this.dict=e}generateIconHTML(e){const t=e.slug&&this.dict[e.slug]
if(t){const r=o(t.icon,{class:`hashtag-color--${this.type}-${e.id}`}),s=document.createElement("span")
return s.classList.add("hashtag-tag-icon"),s.innerHTML=r,t.color&&(s.style.setProperty("--color1",t.color??""),s.style.setProperty("--color2",t.color?a(t.color):"")),s.outerHTML}return super.generateIconHTML(e)}}var p={name:"tag-icons",before:"hashtag-css-generator",initialize(e){(function(...e){const t="string"==typeof e[0]?2:1
e[t]={...e[t],[r]:l},s(...e)})(t=>{t.replaceTagRenderer(u)
const o={}
n.tag_icon_list.split("|").forEach(e=>{const[r,s,i]=e.split(",")
r&&s&&(t.registerCustomTagSectionLinkPrefixIcon&&t.registerCustomTagSectionLinkPrefixIcon({tagName:r,prefixValue:s,prefixColor:i}),o[r]={icon:s,color:i})}),t.registerHashtagType&&t.registerHashtagType("tag",new d(o,e))})}}
const g={"discourse/initializers/tag-icons":Object.freeze({__proto__:null,default:p}),"discourse/lib/colors":c}
export{g as default}

//# sourceMappingURL=722f37c96ae4ae3812c1045b9025729534e0b5a1.map?__ws=www.nodeloc.com
