const{ajax:e}=window.moduleBroker.lookup("discourse/lib/ajax"),{popupAjaxError:t}=window.moduleBroker.lookup("discourse/lib/ajax-error"),{iconHTML:n}=window.moduleBroker.lookup("discourse/lib/icon-library"),{_INTERNAL_SOURCE_KEY:s}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:o}=window.moduleBroker.lookup("discourse/lib/plugin-api"),c=Object.freeze({type:"plugin",name:"checklist"})
const r={nodeSpec:{check:{attrs:{checked:{default:!1}},inline:!0,group:"inline",draggable:!0,selectable:!1,toDOM:e=>["span",{class:e.attrs.checked?"chcklst-box checked fa fa-square-check-o":"chcklst-box fa fa-square-o"}],parseDOM:[{tag:"span.chcklst-box",getAttrs:e=>({checked:l(e.className)})}]}},inputRules:[{match:/(^|\s)\[(x? ?)]$/,handler:(e,t,n,s)=>{const o=e.schema.nodes.check.create({checked:"x"===t[2]}),c=e.schema.text(" ")
return e.tr.replaceWith(n+t[1].length,s,[o,c])}}],parse:{check_open:{node:"check",getAttrs:e=>({checked:l(e.attrGet("class"))})},check_close:{noCloseToken:!0,ignore:!0}},serializeNode:{check:(e,t)=>{e.write(t.attrs.checked?"[x]":"[ ]")}},plugins({pmState:{Plugin:e},pmView:{Decoration:t,DecorationSet:n},schema:s,utils:{changedDescendants:o}}){const c=s.nodes.check,r=s.nodes.list_item,i=s.nodes.bullet_list,l=e=>e?.isTextblock&&e.firstChild?.type===c,a=e=>{for(let t=e.depth;t>0;t--)if(e.node(t).type===r)return e.node(t-1)?.type===i?{bulletListDepth:t-1,listItemDepth:t}:null
return null},d=(e,t)=>null!==a(e.resolve(t)),u=(e,t)=>{if(!t.some(e=>e.docChanged))return null
const{selection:n}=e,{$from:o}=n
if(!n.empty)return null
const r=o.parent
if(!r.isTextblock||0!==r.content.size)return null
const i=a(o)
if(!i)return null
const d=o.node(i.bulletListDepth),u=o.index(i.bulletListDepth)
if(0===u)return null
const h=d.child(u-1).firstChild
if(!l(h))return null
if(h.content.size>2){const t=c.create({checked:!1})
return e.insert(o.pos,[t,s.text(" ")]),e.setSelection(n.constructor.near(e.doc.resolve(o.pos+2))),e}return((e,t)=>{const{bulletListDepth:n,listItemDepth:o}=t,{selection:c}=e,{$from:r}=c,i=r.node(n),l=r.index(n),a=i.child(l-1),d=r.before(n)
e.delete(r.before(o),r.after(o))
let u=1
for(let e=0;e<l-1;e++)u+=i.child(e).nodeSize
const h=e.mapping.map(d+u)
e.delete(h,h+a.nodeSize)
const p=e.mapping.map(d),f=e.doc.nodeAt(p)
if(f&&f.childCount>0){const t=p+f.nodeSize
e.insert(t,s.nodes.paragraph.create()),e.setSelection(c.constructor.near(e.doc.resolve(t+1)))}else e.replaceWith(p,p+(f?.nodeSize||0),s.nodes.paragraph.create()),e.setSelection(c.constructor.near(e.doc.resolve(p+1)))
return e})(e,i)}
return[new e({props:{handleClickOn:(e,t,n,s)=>"check"===n.type.name&&(e.dispatch(e.state.tr.setNodeMarkup(s,null,{checked:!n.attrs.checked})),!0),handleKeyDown(e,t){if("Backspace"!==t.key&&"ArrowLeft"!==t.key)return!1
const{state:n,dispatch:s}=e,{selection:o}=n,{$from:c}=o
if(!o.empty||2!==c.parentOffset||!l(c.parent)||!d(n.doc,c.pos))return!1
if("Backspace"===t.key){const e=a(c),t=c.start()
let o=n.tr.delete(t,t+2)
if(e){const t=o.mapping.map(c.before(e.listItemDepth)),n=o.doc.resolve(t)
n.nodeBefore?.type===r&&(o=o.join(t,2))}return s(o),!0}const i=c.before()
return i>0&&(s(n.tr.setSelection(o.constructor.near(n.doc.resolve(i),-1))),!0)}},appendTransaction(e,t,n){if(e.some(e=>1===e.steps.length&&0===e.steps[0].from&&e.steps[0].to===t.doc.content.size))return null
const c=n.tr
return((e,t,n)=>{const c=[]
o(t.doc,n.doc,(e,t)=>{if(!l(e)||!d(n.doc,t))return
const s=e.childCount>1?e.child(1):null
s?.isText&&" "===s.text?.[0]||c.push(t+1+e.firstChild.nodeSize)})
for(let t=c.length-1;t>=0;t--)e.insert(c[t],s.text(" "))})(c,t,n),u(c,e)??(e=>{const{doc:t,selection:n}=e,{$from:s}=n
if(!n.empty)return null
const o=s.parent
if(!l(o)||!d(t,s.pos))return null
const c=o.firstChild.nodeSize,r=o.childCount>1?o.child(1):null,i=r?.isText&&" "===r.text?.[0]?c+1:c
return s.parentOffset<i?(e.setSelection(n.constructor.near(t.resolve(s.start()+i))),e):null})(c)??(c.docChanged?c:null)}}),new e({props:{decorations(e){const s=[]
return e.doc.descendants((e,n,o)=>{e.type===r&&o?.type===i&&l(e.firstChild)&&s.push(t.node(n,n+e.nodeSize,{class:"has-checkbox"}))}),n.create(e.doc,s)}}})]},commands:({schema:e,pmSchemaList:t})=>{const n=e.nodes.check,s=e.nodes.bullet_list,o=e.nodes.ordered_list,c=e.nodes.list_item,r=e=>{const t=e.firstChild
return t?.isTextblock&&t.firstChild?.type===n},i=e=>{const{$from:t,$to:n}=e.selection
for(let e=t.depth;e>0;e--)if(t.node(e).type===c){const c=t.node(e-1)
if(c?.type===s||c?.type===o)return{listDepth:e-1,listItemDepth:e,listType:c.type,list:c,listStart:t.before(e-1),$from:t,$to:n}}return null},l=(e,t)=>{const{list:n,listStart:s,$from:o,$to:c}=e,r=o.pos===c.pos
n.forEach((e,n)=>{const i=s+1+n,l=i+e.nodeSize;(r?o.pos>=i&&o.pos<=l:!(l<=o.pos||i>=c.pos))&&t(e,i)})},a=e=>{if(!e||e.listType!==s)return!1
let t=!1
return l(e,e=>{r(e)&&(t=!0)}),t},d=(e,t)=>{const n=[]
l(t,(e,t)=>{if(r(e)){const s=e.firstChild,o=s.firstChild.nodeSize,c=s.childCount>1?s.child(1):null,r=c?.isText&&" "===c.text?.[0]
n.push({from:t+2,to:t+2+o+(r?1:0)})}})
let s=e.tr
for(let e=n.length-1;e>=0;e--)s=s.delete(n[e].from,n[e].to)
return s},u=(t,s)=>{const o=[]
l(s,(e,t)=>{!r(e)&&e.firstChild?.isTextblock&&o.push(t+2)})
let c=t.tr,i=0
for(const t of o){const s=n.create({checked:!1}),o=e.text(" ")
c=c.insert(t+i,[s,o]),i+=s.nodeSize+o.nodeSize}return c}
return{toggleBulletList:()=>(e,t)=>{const n=i(e)
return!!a(n)&&(t&&t(d(e,n)),!0)},toggleOrderedList:()=>(e,n,s)=>{const r=i(e)
if(!a(r))return!1
if(!n)return!0
const l=t?.liftListItem,u=t?.wrapInList
return!(!l||!u)&&(n(d(e,r)),s&&(l(c)(s.state,n),u(o)(s.state,n)),!0)},toggleChecklist:()=>(e,n,r)=>{const l=i(e),h=t?.wrapInList,p=t?.liftListItem
if(a(l))return n&&p?(n(d(e,l)),r&&p(c)(r.state,n),!0):!!p
if(l?.listType===s)return n&&n(u(e,l)),!0
if(l?.listType===o){if(!n||!p||!h)return!(!p||!h)
if(p(c)(e,n),r){h(s)(r.state,n)
const e=i(r.state)
e&&n(u(r.state,e))}return!0}if(!h)return!1
if(!n)return h(s)(e,void 0)
if(h(s)(e,n),r){const e=i(r.state)
e&&n(u(r.state,e))}return!0}}},state:({schema:e,utils:{inNode:t}},n)=>{const{$from:s}=n.selection
let o=!1
for(let t=s.depth;t>0;t--){const n=s.node(t)
if(n.type===e.nodes.list_item){if(s.node(t-1)?.type===e.nodes.bullet_list){const t=n.firstChild
o=t?.isTextblock&&t.firstChild?.type===e.nodes.check}break}}return{inCheckList:o,inBulletList:!o&&t(n,e.nodes.bullet_list)}}},i=/\bchecked\b/
function l(e){return i.test(e)}var a=Object.freeze({__proto__:null,default:r})
function d(e){return 3===e.nodeType&&e.nodeValue.match(/^\s*$/)}function u(e){e.forEach(e=>{let t=e.parentElement
"P"===t.nodeName&&t.parentElement.firstElementChild===t&&(t=t.parentElement),"LI"!==t.nodeName||"UL"!==t.parentElement.nodeName||function(e){let t=e.previousSibling
for(;t;){if(!d(t))return!0
t=t.previousSibling}return!1}(e)||(t.classList.add("has-checkbox"),e.classList.add("list-item-checkbox"),e.nextSibling||e.insertAdjacentHTML("afterend","&#8203;"))})}function h(s,o){const c=[...s.getElementsByClassName("chcklst-box")]
u(c)
const r=o?.getModel()
r?.can_edit&&c.forEach((s,o)=>{s.onclick=async s=>{const i=s.currentTarget,l=i.classList
if(l.contains("permanent")||l.contains("readonly"))return
const a=l.contains("checked")?"[ ]":"[x]",d=document.createElement("template")
d.innerHTML=n("spinner",{class:"fa-spin list-item-checkbox"}),i.insertAdjacentElement("afterend",d.content.firstChild),i.classList.add("hidden"),c.forEach(e=>e.classList.add("readonly"))
try{const t=await e(`/posts/${r.id}`),n=[];[/`[^`\n]*\n?[^`\n]*`/gm,/^```[^]*?^```/gm,/\[code\][^]*?\[\/code\]/gm,/_(?=\S).*?\S_/gm,/~~(?=\S).*?\S~~/gm].forEach(e=>{let s
for(;null!=(s=e.exec(t.raw));)n.push([s.index,s.index+s[0].length])}),[/([^\[\n]|^)\*\S.+?\S\*(?=[^\]\n]|$)/gm].forEach(e=>{let s
for(;null!=(s=e.exec(t.raw));)n.push([s.index+1,s.index+s[0].length])})
let s=-1,c=!1
const i=t.raw.replace(/\[( |x)?\]/gi,(e,r,i)=>c||i>0&&"!"===t.raw[i-1]||i>0&&"\\"===t.raw[i-1]?e:(s+=n.every(t=>t[0]>=i+e.length||i>t[1]),s===o?(c=!0,a):e))
await r.save({raw:i})}catch(e){t(e)}finally{c.forEach(e=>e.classList.remove("readonly")),i.classList.remove("hidden"),i.parentElement.querySelector(".fa-spin").remove()}}})}var p={name:"checklist",initialize(){(function(...e){const t="string"==typeof e[0]?2:1
e[t]={...e[t],[s]:c},o(...e)})(e=>function(e){e.container.lookup("service:site-settings").checklist_enabled&&(e.decorateCookedElement(h),e.registerRichEditorExtension(r),e.addComposerToolbarPopupMenuOption({menu:"list",name:"list-checklist",icon:"list-check",label:"checklist.composer.checklist",showActiveIcon:!0,active:({state:e})=>e?.inCheckList,action:e=>{e.commands?.toggleChecklist?e.commands.toggleChecklist():e.applyList("- [ ] ","list_item")}}))}(e))}}
const f=/\[( |x)?\]/gi
function k(e,t,n,s){const o=function(e){switch(e){case"x":return"checked fa fa-square-check-o"
case"X":return"checked permanent fa fa-square-check"
default:return"fa fa-square-o"}}(n[1]),c=new s.Token("check_open","span",1)
c.attrs=[["class",`chcklst-box ${o}`]],e.push(c)
const r=new s.Token("check_close","span",-1)
e.push(r)}function m(e,t){let n,s=null,o=0
for(;n=f.exec(e);){if(n.index>o){s=s||[]
const c=new t.Token("text","",0)
c.content=e.slice(o,n.index),s.push(c)}o=n.index+n[0].length,s=s||[],k(s,0,n,t)}if(s&&o<e.length){const n=new t.Token("text","",0)
n.content=e.slice(o),s.push(n)}return s}function g(e){let t,n,s,o,c,r=e.tokens,i=0
for(n=0,s=r.length;n<s;n++)if("inline"===r[n].type)for(o=r[n].children,t=o.length-1;t>=0;t--)if(c=o[t],i+=c.nesting,"text"===c.type&&0===i){const s=m(c.content,e)
s&&(r[n].children=o=e.md.utils.arrayReplaceAt(o,t,s))}}const x={"discourse/initializers/checklist":Object.freeze({__proto__:null,checklistSyntax:h,default:p}),"lib/discourse-markdown/checklist":Object.freeze({__proto__:null,setup:function(e){e.registerOptions((e,t)=>{e.features.checklist=!!t.checklist_enabled}),e.allowList(["span.chcklst-stroked","span.chcklst-box fa fa-square-o","span.chcklst-box checked fa fa-square-check-o","span.chcklst-box checked permanent fa fa-square-check"]),e.registerPlugin(e=>e.core.ruler.before("text_join","checklist",g))}}),"lib/rich-editor-extension":a}
export{x as default}

//# sourceMappingURL=../../map/plugins/checklist_main.CvZH4z-hm548whp.digested.js.map
