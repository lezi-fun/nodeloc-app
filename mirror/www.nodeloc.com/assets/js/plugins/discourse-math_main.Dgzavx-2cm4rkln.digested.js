const{default:t}=window.moduleBroker.lookup("@glimmer/component"),{tracked:e}=window.moduleBroker.lookup("@glimmer/tracking"),{action:n}=window.moduleBroker.lookup("@ember/object"),{default:i}=window.moduleBroker.lookup("discourse/components/form"),{default:o}=window.moduleBroker.lookup("discourse/ui-kit/d-button"),{default:a}=window.moduleBroker.lookup("discourse/ui-kit/d-modal"),{i18n:r}=window.moduleBroker.lookup("discourse-i18n"),{setComponentTemplate:s}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:l}=window.moduleBroker.lookup("@ember/template-factory"),{on:c}=window.moduleBroker.lookup("@ember/modifier"),{default:u}=window.moduleBroker.lookup("discourse/ui-kit/d-toggle-switch"),{_INTERNAL_SOURCE_KEY:d,apiInitializer:m}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:h}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{getExtensions:p}=window.moduleBroker.lookup("discourse/lib/composer/rich-editor-extensions"),{iconHTML:f}=window.moduleBroker.lookup("discourse/lib/icon-library"),{warn:_}=window.moduleBroker.lookup("@ember/debug"),{cancel:b,later:g}=window.moduleBroker.lookup("@ember/runloop"),{isTesting:x}=window.moduleBroker.lookup("discourse/lib/environment"),{getURLWithCDN:k}=window.moduleBroker.lookup("discourse/lib/get-url"),{sanitize:w}=window.moduleBroker.lookup("discourse/lib/text"),{default:y}=window.moduleBroker.lookup("discourse/lib/load-script"),{helperContext:v}=window.moduleBroker.lookup("discourse/lib/helpers")
class A extends t{static{dt7948.g(this.prototype,"formApi",[e])}#t=void dt7948.i(this,"formApi")
get initialData(){return{text:this.args.model?.initialText??""}}onSubmit(t){this.args.model?.onApply?.(t.text??""),this.args.closeModal()}static{dt7948.n(this.prototype,"onSubmit",[n])}onRegisterApi(t){this.formApi=t}static{dt7948.n(this.prototype,"onRegisterApi",[n])}submitForm(){this.formApi?.submit()}static{dt7948.n(this.prototype,"submitForm",[n])}cancel(){this.args.closeModal()}static{dt7948.n(this.prototype,"cancel",[n])}static{s(l({id:null,block:'[[[8,[32,0],[[24,0,"math-edit-modal"]],[["@title","@closeModal"],[[28,[32,1],["discourse_math.edit_modal.title"],null],[30,1]]],[["body","footer"],[[[[1,"\\n    "],[8,[32,2],null,[["@data","@onSubmit","@onRegisterApi"],[[30,0,["initialData"]],[30,0,["onSubmit"]],[30,0,["onRegisterApi"]]]],[["default"],[[[[1,"\\n      "],[8,[30,2,["Field"]],null,[["@name","@title","@format","@type"],["text",[28,[32,1],["discourse_math.edit_modal.label"],null],"full","textarea"]],[["default"],[[[[1,"\\n        "],[8,[30,3,["Control"]],[[24,0,"math-edit-modal__textarea"],[16,"autofocus",true]],null,null],[1,"\\n      "]],[3]]]]],[1,"\\n    "]],[2]]]]],[1,"\\n  "]],[]],[[[1,"\\n    "],[8,[32,3],[[24,0,"btn-primary math-edit-modal__apply"]],[["@action","@label"],[[30,0,["submitForm"]],"discourse_math.edit_modal.apply"]],null],[1,"\\n    "],[8,[32,3],[[24,0,"btn-default"]],[["@action","@label"],[[30,0,["cancel"]],"cancel"]],null],[1,"\\n  "]],[]]]]]],["@closeModal","form","field"],[]]',moduleName:"(unknown template module)",scope:()=>[a,r,i,o],isStrictMode:!0}),this)}}var C=Object.freeze({__proto__:null,default:A})
class M extends t{static{dt7948.g(this.prototype,"formApi",[e])}#t=void dt7948.i(this,"formApi")
static{dt7948.g(this.prototype,"isBlock",[e])}#e=void dt7948.i(this,"isBlock")
constructor(){super(...arguments),this.isBlock=this.args.model?.isBlock??!1}get initialData(){return{text:""}}get modalTitle(){return this.isBlock?r("discourse_math.insert_modal.title_block"):r("discourse_math.insert_modal.title_inline")}onSubmit(t){const e=t.text?.trim()??""
e&&this.args.model?.onInsert?.(e,this.isBlock),this.args.closeModal()}static{dt7948.n(this.prototype,"onSubmit",[n])}onRegisterApi(t){this.formApi=t}static{dt7948.n(this.prototype,"onRegisterApi",[n])}submitForm(){this.formApi?.submit()}static{dt7948.n(this.prototype,"submitForm",[n])}cancel(){this.args.closeModal()}static{dt7948.n(this.prototype,"cancel",[n])}toggleBlockMode(){this.isBlock=!this.isBlock}static{dt7948.n(this.prototype,"toggleBlockMode",[n])}static{s(l({id:null,block:'[[[8,[32,0],[[24,0,"math-insert-modal"]],[["@title","@closeModal"],[[30,0,["modalTitle"]],[30,1]]],[["body","footer"],[[[[1,"\\n    "],[10,0],[14,0,"math-insert-modal__toggle"],[12],[1,"\\n      "],[8,[32,1],[[4,[32,2],["click",[30,0,["toggleBlockMode"]]],null]],[["@state","@label"],[[30,0,["isBlock"]],"discourse_math.insert_modal.block_mode"]],null],[1,"\\n    "],[13],[1,"\\n    "],[8,[32,3],null,[["@data","@onSubmit","@onRegisterApi"],[[30,0,["initialData"]],[30,0,["onSubmit"]],[30,0,["onRegisterApi"]]]],[["default"],[[[[1,"\\n      "],[8,[30,2,["Field"]],null,[["@name","@title","@format","@validation","@type"],["text",[28,[32,4],["discourse_math.insert_modal.label"],null],"full","required","textarea"]],[["default"],[[[[1,"\\n        "],[8,[30,3,["Control"]],[[24,0,"math-insert-modal__textarea"],[16,"placeholder",[28,[32,4],["discourse_math.insert_modal.placeholder"],null]],[16,"autofocus",true]],null,null],[1,"\\n      "]],[3]]]]],[1,"\\n    "]],[2]]]]],[1,"\\n  "]],[]],[[[1,"\\n    "],[8,[32,5],[[24,0,"btn-primary math-insert-modal__insert"]],[["@action","@label"],[[30,0,["submitForm"]],"discourse_math.insert_modal.insert"]],null],[1,"\\n    "],[8,[32,5],[[24,0,"btn-default"]],[["@action","@label"],[[30,0,["cancel"]],"cancel"]],null],[1,"\\n  "]],[]]]]]],["@closeModal","form","field"],[]]',moduleName:"(unknown template module)",scope:()=>[a,u,c,i,r,o],isStrictMode:!0}),this)}}var B=Object.freeze({__proto__:null,default:M})
const $=Object.freeze({type:"plugin",name:"discourse-math"})
var S={name:"math-admin-plugin-configuration-nav",initialize(t){const e=t.lookup("service:current-user")
e?.admin&&function(...t){const e="string"==typeof t[0]?2:1
t[e]={...t[e],[d]:$},h(...t)}(t=>{t.setAdminPluginIcon("discourse-math","square-root-variable")})}},T=Object.freeze({__proto__:null,default:S})
const j=Object.freeze({type:"plugin",name:"discourse-math"})
function z(...t){const e="string"==typeof t[0]?2:1
return t[e]={...t[e],[d]:j},m(...t)}function L(){const t=v()
return t?.site?.discourse_math_bundle_url||"/plugins/discourse-math"}function O(){return`${L()}/mathjax`}function E(){return`${L()}/katex`}var N=Object.freeze({__proto__:null,getKaTeXBasePath:E,getMathJaxBasePath:O})
function P(){return E()}let I,R,q,D
async function J(){return R||(R=y(k(`${P()}/katex.min.css`),{css:!0})),await R,I||(I=y(k(`${P()}/katex.min.js`))),await I,window.katex}function F(){return q||(q=y(k(`${P()}/contrib/mhchem.min.js`))),q}function W(){return D||(D=y(k(`${P()}/contrib/copy-tex.min.js`))),D}function H(){return window.katex}var K=Object.freeze({__proto__:null,getKaTeX:H,loadCopyTex:W,loadKaTeX:J,loadMhchem:F})
let X,U,V
async function Z(t={}){return X||(X=J()),await X,t.enableMhchem&&!U&&(U=F()),t.enableCopyTex&&!V&&(V=W()),await Promise.all([U,V].filter(Boolean)),H()??window.katex}var Y=Object.freeze({__proto__:null,default:Z})
let G
function Q(t){if(!G){const e="svg"===t?"tex-mml-svg.js":"tex-mml-chtml.js"
G=y(k(`${O()}/${e}`))}return G}function tt(){return window.MathJax}var et=Object.freeze({__proto__:null,getMathJax:tt,loadOutput:Q})
let nt
async function it(t={}){const e="svg"===t.output?"svg":"html"
return nt||(nt=Q(e)),await nt,await window.MathJax.startup.promise,tt()??window.MathJax}var ot=Object.freeze({__proto__:null,default:it})
const at="math-hidden",rt="math-applied-mathjax",st="math-applied-katex",lt="data-math-original",ct=new WeakMap,ut=/^[A-Za-z_][A-Za-z0-9_.:-]*$/
function dt(t,e){if(x())return
const n=e?.message?` (${e.message})`:""
_(`discourse-math: ${t}${n}`,!1,{id:"discourse-math.render"})}const mt=new class{#n=!1
#i=null
reset(){this.#n=!1,this.#i=null}isInitializedWith(t){return this.#n&&this.#i===t}markInitialized(t){this.#n=!0,this.#i=t}}
function ht(t){const e=t.discourse_math_provider
return{enabled:t.discourse_math_enabled,provider:e,enable_menu:t.discourse_math_enable_menu,enable_asciimath:t.discourse_math_enable_asciimath&&"mathjax"===e,enable_accessibility:t.discourse_math_enable_accessibility,mathjax_output:t.discourse_math_mathjax_output,zoom_on_click:t.discourse_math_zoom_on_click}}function pt(t){const e=function(t){return JSON.stringify({output:t.mathjax_output,a11y:t.enable_accessibility,zoom:t.zoom_on_click,ascii:t.enable_asciimath,menu:t.enable_menu})}(t)
mt.isInitializedWith(e)||(window.MathJax=function(t){const e=O(),n={startup:{typeset:!1,ready:()=>window.MathJax?.startup?.defaultReady?.()},chtml:{},svg:{},loader:{load:["ui/safe"],paths:{mathjax:k(e)}},options:{menuOptions:{settings:{}}},tex:{inlineMath:[["\\(","\\)"]],displayMath:[["\\[","\\]"]]},asciimath:{delimiters:[["%","%"]]}}
return"html"===t.mathjax_output?n.chtml.fontURL=k(`${e}/woff-v2`):"svg"===t.mathjax_output&&(n.svg.fontCache="global"),t.enable_menu?n.options.enableMenu=!0:n.options.enableMenu=!1,n.options.menuOptions.settings={enrich:Boolean(t.enable_accessibility)},t.zoom_on_click&&(n.options.menuOptions.settings.zoom="Click",n.options.menuOptions.settings.zscale="175%"),t.enable_asciimath&&n.loader.load.push("input/asciimath"),n}(t),mt.markInitialized(e))}function ft(t,e,n){const i=document.createElement(t)
return i.className=`${e} ${at}`,i.setAttribute("hidden",""),i.textContent=n,i}function _t(t,e){const n=e.enable_asciimath?".math, .asciimath":".math",i=t.querySelectorAll(n),o=[]
return i.forEach(t=>{if(t.classList.contains(rt))return
t.classList.add(rt)
const e=function(t){if(t.classList.contains("math")){const e="DIV"===t.tagName?"div":"span",n="div"===e?["\\[","\\]"]:["\\(","\\)"],i=ft(e,`math-container ${"div"===e?"block-math":"inline-math"} mathjax-math`,`${n[0]}${t.textContent}${n[1]}`)
return t.after(i),i}if(t.classList.contains("asciimath")){const e=ft("span","math-container inline-math ascii-math mathjax-math",`%${t.textContent}%`)
return t.after(e),e}return null}(t)
e&&o.push({original:t,wrapper:e})}),o}function bt(t){return t.filter(({original:t,wrapper:e})=>e?.isConnected&&function(t){if(!t?.isConnected)return!1
if(t.closest("[hidden]"))return!1
const e=window.getComputedStyle(t)
return"none"!==e.display&&"hidden"!==e.visibility}(t))}function gt(t){t.forEach(({original:t,wrapper:e})=>{var n
n=t,n?.isConnected&&(n.classList.add(at),n.setAttribute("hidden","")),e?.isConnected&&(function(t){t.querySelectorAll("mjx-container").forEach(t=>{t.style.removeProperty("display")})}(e),function(t){t?.isConnected&&(t.classList.remove(at),t.removeAttribute("hidden"))}(e))})}async function xt(t,e){try{const n=await async function(t){return pt(t),await it({output:t.mathjax_output})}(e),i=bt(t)
if(0===i.length||!n?.typesetPromise)return
await n.typesetPromise(i.map(({wrapper:t})=>t)),gt(i)}catch(e){dt("MathJax rendering failed",e),function(t){t.forEach(({original:t,wrapper:e})=>{t?.isConnected&&(t.classList.remove(at,rt),t.removeAttribute("hidden")),e?.isConnected&&e.remove()})}(t)}}function kt(t,e,n={}){if(!t)return
n.force&&(function(t){const e=ct.get(t)
e&&(b(e.timer),ct.delete(t))}(t),function(t,e){const n=e.enable_asciimath?".math, .asciimath":".math"
t.querySelectorAll(n).forEach(t=>{t.classList.remove(rt,at),t.removeAttribute("hidden")}),t.querySelectorAll(".math-container.mathjax-math").forEach(t=>t.remove())}(t,e))
const i=_t(t,e)
if(0===i.length)return
const o=t.classList.contains("d-editor-preview");(function(t,e,n,i){const o=ct.get(t)
if(o){b(o.timer)
const a=new Set(o.wrappers.map(({wrapper:t})=>t))
return e.forEach(t=>{a.has(t.wrapper)||(o.wrappers.push(t),a.add(t.wrapper))}),o.opts=n,void(o.timer=g(()=>{ct.delete(t),xt(o.wrappers,o.opts)},i))}const a={wrappers:[...e],opts:n,timer:null}
a.timer=g(()=>{ct.delete(t),xt(a.wrappers,a.opts)},i),ct.set(t,a)})(t,i,e,o?200:0)}async function wt(t,e={}){if(!t)return
const n=t.querySelectorAll(".math")
if(0===n.length)return
e.force&&function(t){t.querySelectorAll(".math").forEach(t=>{const e=t.getAttribute(lt),n=!!t.querySelector(".katex")
e&&n&&(t.textContent=e),t.removeAttribute(lt),t.classList.remove(st,"math-container","inline-math","block-math","katex-math")})}(t)
try{await async function(){x()&&window.katex||await Z({enableMhchem:!0,enableCopyTex:!0})}()}catch(t){return void dt("KaTeX failed to load",t)}const i={trust:t=>{if("\\href"===t.command)return Boolean(function(t){if(!t||/[<>"']/.test(t))return null
try{const e=new URL(t,window.location.origin),n=t.startsWith("/")||t.startsWith("#"),i=["http:","https:","mailto:"].includes(e.protocol)
if(!n&&!i)return null
const o=w(t)
return!o||""===o.trim()||o.includes("&gt;")||o.includes("&lt;")?null:o}catch{return null}}(t.url))
if("\\htmlId"===t.command){const n=t.url||t.text||t.id
return e=n,Boolean(e)&&ut.test(e)}var e
return!1},macros:{"\\eqref":"\\href{###1}{(\\text{#1})}","\\ref":"\\href{###1}{\\text{#1}}","\\label":"\\htmlId{#1}{}"},displayMode:!1}
n.forEach(t=>function(t,e){if(t.classList.contains(st))return
if(!t.classList.contains("math"))return
t.classList.add(st)
const n="DIV"===t.tagName,i=n?"block-math":"inline-math",o=t.querySelector(".katex")?t.getAttribute(lt):t.textContent,a=t.querySelector("annotation[encoding='application/x-tex']")?.textContent,r=o??a??t.textContent??""
t.setAttribute(lt,r),t.classList.add("math-container",i,"katex-math"),t.textContent=""
try{window.katex.render(r,t,{...e,displayMode:n})}catch(e){dt("KaTeX rendering failed",e),t.textContent=r,t.removeAttribute(lt),t.classList.remove(st,"math-container",i,"katex-math")}}(t,i))}function yt(t,e,n={}){t&&e?.enabled&&(x()||("mathjax"!==e.provider?"katex"===e.provider&&wt(t,n):kt(t,e,n)))}var vt=Object.freeze({__proto__:null,buildDiscourseMathOptions:ht,renderKatex:wt,renderMathInElement:yt,renderMathJax:kt,resetMathJaxState:function(){mt.reset()}})
const At=({getContext:t,pmState:{NodeSelection:e}})=>(n,i,o)=>new Mt({node:n,view:i,getPos:o,getContext:t,NodeSelection:e})
function Ct(t,e){if(!t)return""
let n=""
for(let i=0;i<t.length;i++){const o=t[i]
if(o===e){let e=0,o=i-1
for(;o>=0&&"\\"===t[o];)e++,o--
e%2==0&&(n+="\\")}n+=o}return n}class Mt{node
view
getPos
getContext
NodeSelection
dom
editButton
content
openEditModal=t=>{t.preventDefault(),t.stopPropagation()
const{modal:e}=this.getContext()
e.show(A,{model:{initialText:this.node.attrs.text??"",isBlock:!this.node.isInline,mathType:this.node.attrs.mathType??"tex",onApply:t=>this.#o(t)}})}
constructor({node:t,view:e,getPos:n,getContext:i,NodeSelection:o}){this.node=t,this.view=e,this.getPos=n,this.getContext=i,this.NodeSelection=o
const a=t.isInline
this.dom=document.createElement(a?"span":"div"),this.dom.classList.add("composer-math-node"),this.editButton=document.createElement("button"),this.editButton.type="button",this.editButton.classList.add("btn-flat","math-node-edit-button"),this.editButton.setAttribute("contenteditable","false"),this.editButton.setAttribute("title",r("discourse_math.edit_math")),this.editButton.setAttribute("aria-label",r("discourse_math.edit_math")),this.editButton.innerHTML=f("pencil"),this.editButton.addEventListener("click",this.openEditModal),this.content=document.createElement(a?"span":"div"),this.content.classList.add("math-node-content"),this.content.setAttribute("contenteditable","false"),this.dom.appendChild(this.editButton),this.dom.appendChild(this.content),this.#a(),this.#r(!0)}update(t){const e=t.attrs.text!==this.node.attrs.text||t.attrs.mathType!==this.node.attrs.mathType
return this.node=t,e&&(this.#a(),this.#r(!0)),!0}selectNode(){this.dom.classList.add("ProseMirror-selectednode")}deselectNode(){this.dom.classList.remove("ProseMirror-selectednode")}stopEvent(t){return t.target instanceof Node&&this.editButton.contains(t.target)}ignoreMutation(){return!0}destroy(){this.editButton.removeEventListener("click",this.openEditModal)}#a(){const t=this.node.isInline&&"asciimath"===this.node.attrs.mathType
this.content.classList.toggle("asciimath",t),this.content.classList.toggle("math",!t),this.content.textContent=this.node.attrs.text??""}#r(t=!1){const e=ht(this.getContext().siteSettings)
yt(this.dom,e,{force:t})}#o(t){const e=this.getPos(),n={...this.node.attrs,text:t},i=this.view.state.tr.setNodeMarkup(e,null,n)
i.setSelection(this.NodeSelection.create(i.doc,e)),this.view.dispatch(i)}}const Bt={nodeViews:{math_inline:At,math_block:At},nodeSpec:{math_inline:{inline:!0,group:"inline",atom:!0,selectable:!0,draggable:!0,attrs:{text:{default:""},mathType:{default:"tex"}},parseDOM:[{tag:"span.math",getAttrs:t=>({text:t.textContent.trim(),mathType:"tex"})},{tag:"span.asciimath",getAttrs:t=>({text:t.textContent.trim(),mathType:"asciimath"})}],toDOM:t=>["span",{class:"asciimath"===t.attrs.mathType?"asciimath":"math"},t.attrs.text]},math_block:{group:"block",atom:!0,selectable:!0,defining:!0,isolating:!0,attrs:{text:{default:""},mathType:{default:"tex"}},parseDOM:[{tag:"div.math",getAttrs:t=>({text:t.textContent.trim(),mathType:"tex"})}],toDOM:t=>["div",{class:"math"},t.attrs.text]}},parse:{math_inline:{node:"math_inline",getAttrs:t=>({text:t.content,mathType:t.meta?.mathType||"tex"})},math_block:{node:"math_block",getAttrs:t=>({text:t.content,mathType:t.meta?.mathType||"tex"})}},serializeNode:({utils:{isBoundary:t}})=>({math_inline(e,n,i,o){e.flushClose(),t(e.out,e.out.length-1)||e.write(" ")
const a="asciimath"===n.attrs.mathType?"%":"$",r=Ct(n.attrs.text??"",a)
e.write(`${a}${r}${a}`)
const s=i.childCount>o+1?i.child(o+1):null
s?.isText&&!t(s.text,0)&&e.write(" ")},math_block(t,e){t.ensureNewLine()
const n=Ct(e.attrs.text??"","$")
t.write("$$\n"),t.write(n),t.write("\n$$\n\n")}})}
var $t=Object.freeze({__proto__:null,default:Bt}),St=z(t=>{p().includes(Bt)||t.registerRichEditorExtension(Bt)}),Tt=Object.freeze({__proto__:null,default:St})
var jt=z(t=>{const e=ht(t.container.lookup("service:site-settings"))
e.enabled&&function(t,e){const n=e.provider
t.decorateCookedElement(t=>yt(t,e),{id:n}),t.decorateChatMessage&&t.decorateChatMessage(t=>yt(t,e),{id:`${n}-chat`})
const i=t.container.lookup("service:modal")
t.addComposerToolbarPopupMenuOption({name:"insert-math",label:"discourse_math.composer.insert_math",icon:"square-root-variable",shortcut:"Shift+M",action:t=>{const e=function(t){if(!t)return!0
const e=t.lastIndexOf("\n")
return""===(-1===e?t:t.slice(e+1)).trim()}(t.selected.pre)
i.show(M,{model:{isBlock:e,onInsert:(e,n)=>{n?t.addText(`$$\n${e}\n$$\n`):t.addText(`$${e}$`)}}})}})}(t,e)})
const zt=36,Lt=37,Ot=92,Et=91,Nt=93,Pt="tex",It="asciimath",Rt="math_inline",qt="math_block",Dt="math",Jt="asciimath",Ft=[12289,12290,65292,65306,65307,65294,65311,65281,1548,1563,1567,3631]
function Wt(t,e,n){return t!==e&&(!!n.utils.isWhiteSpace(t)||(!!n.utils.isMdAsciiPunct(t)||(!!n.utils.isPunctChar(t)||!!Ft.includes(t))))}function Ht(t,e,n){const i=t.push(Rt,"",0)
i.content=e,i.meta={mathType:n}}function Kt(t,e){const n=t.push(qt,"",0)
n.content=e,n.block=!0}function Xt(t,e){let n=0,i=e-1
for(;i>=0&&t.charCodeAt(i)===Ot;)n++,i--
return n%2==1}function Ut(t,e,n){const i=t.pos,o=t.posMax
if(e||t.src.charCodeAt(i)!==n||o<i+2)return!1
if(t.src.charCodeAt(i+1)===n)return!1
if(i>0){const e=t.src.charCodeAt(i-1)
if(!Wt(e,n,t.md))return!1
if(e===n)return!1}const a=function(t,e,n,i){for(let o=e;o<n;o++)if(t.charCodeAt(o)===i&&!Xt(t,o))return o
return-1}(t.src,i+1,o,n)
if(-1===a)return!1
if(a+1<=o){const e=t.src.charCodeAt(a+1)
if(e&&!Wt(e,n,t.md))return!1
if(e===n)return!1}const r=t.src.slice(i+1,a)
if(r.includes("\n"))return!1
return Ht(t,r,n===zt?Pt:It),t.pos=a+1,!0}function Vt(t,e,n,i){const o=t.pos,a=t.posMax
if(e||a<o+n.length)return!1
if(t.src.slice(o,o+n.length)!==n)return!1
const r=o+n.length,s=function(t,e,n){const i=n.length
for(let o=e;o<=t.length-i;o++){if(t.slice(o,o+i)!==n)continue
let e=0,a=o-1
for(;a>=0&&t.charCodeAt(a)===Ot;)e++,a--
if(e%2==0)return o}return-1}(t.src,r,i)
if(-1===s)return!1
const l=t.src.slice(r,s)
return!(!l||l.includes("\n"))&&(Ht(t,l,Pt),t.pos=s+i.length,!0)}function Zt(t,e){return Ut(t,e,zt)}function Yt(t,e){return Vt(t,e,"\\(","\\)")}function Gt(t,e){return Ut(t,e,Lt)}function Qt(t,e,n,i){for(let o=e;o<n;o++)if(!i.utils.isSpace(t.charCodeAt(o)))return!1
return!0}function te(t,e,n,i){return t.src.charCodeAt(e)===zt&&(t.src.charCodeAt(e+1)===zt&&Qt(t.src,e+2,n,i))}function ee(t,e,n,i){return t.src.charCodeAt(e)===Ot&&(t.src.charCodeAt(e+1)===Nt&&Qt(t.src,e+2,n,i))}function ne(t,e,n,i,o){if(i)return!0
const{nextLine:a,closed:r}=function(t,e,n,i){let o=e
for(;;){if(o++,o>=n)return{nextLine:o,closed:!1}
if(i(t,t.bMarks[o]+t.tShift[o],t.eMarks[o],t.md))return{nextLine:o,closed:!0}}}(t,e,n,o),s=function(t,e,n,i){const o=t.bMarks[e+1]+t.tShift[e+1],a=i?t.eMarks[n-1]:t.eMarks[n]
return t.src.slice(o,a)}(t,e,a,r)
return Kt(t,s),t.line=r?a+1:a,!0}function ie(t,e,n,i){const o=t.bMarks[e]+t.tShift[e],a=t.eMarks[e],r=t.md.options.discourse.features.enable_latex_delimiters,s=t.src.slice(o,a).trim(),l=function(t,e,n,i,o){const a=[{start:"$$",end:"$$"}]
o&&a.push({start:"\\[",end:"\\]"})
for(const{start:o,end:r}of a)if(n.startsWith(o)&&n.endsWith(r)&&n.length>o.length+r.length){if(i)return!0
const a=n.slice(o.length,-r.length).trim()
return!!a&&(Kt(t,a),t.line=e+1,!0)}return null}(t,e,s,i,r)
return null!==l?l:te(t,o,a,t.md)?ne(t,e,n,i,te):!(!r||!function(t,e,n,i){return t.src.charCodeAt(e)===Ot&&t.src.charCodeAt(e+1)===Et&&Qt(t.src,e+2,n,i)}(t,o,a,t.md))&&ne(t,e,n,i,ee)}const oe={"discourse/components/modal/math-edit":C,"discourse/components/modal/math-insert":B,"discourse/initializers/math-admin-plugin-configuration-nav":T,"initializers/discourse-math-rich-editor":Tt,"initializers/discourse-math":Object.freeze({__proto__:null,default:jt}),"lib/discourse-markdown/discourse-math":Object.freeze({__proto__:null,setup:function(t){t.markdownIt&&(t.allowList([`span.${Dt}`,`span.${Jt}`,`div.${Dt}`]),t.registerOptions((t,e)=>{t.features.math=e.discourse_math_enabled,t.features.asciimath=e.discourse_math_enable_asciimath&&"mathjax"===e.discourse_math_provider,t.features.enable_latex_delimiters=e.discourse_math_enable_latex_delimiters}),t.registerPlugin(t=>{t.options.discourse.features.math&&(t.renderer.rules[Rt]=(e,n)=>{const i=e[n],o=i.meta?.mathType
return`<span class='${o===It?Jt:Dt}'>${t.utils.escapeHtml(i.content)}</span>`},t.renderer.rules[qt]=(e,n)=>{const i=e[n],o=t.utils.escapeHtml(i.content)
return`<div class='${Dt}'>\n${o}\n</div>\n`},t.options.discourse.features.asciimath&&t.inline.ruler.after("escape",Jt,Gt),t.options.discourse.features.enable_latex_delimiters&&t.inline.ruler.before("text","math-paren",Yt),t.inline.ruler.after("escape",Dt,Zt),t.block.ruler.after("code",Dt,ie,{alt:["paragraph","reference","blockquote","list"]}))}))}}),"lib/katex-bundle":K,"lib/load-katex":Y,"lib/load-mathjax":ot,"lib/math-bundle-paths":N,"lib/math-renderer":vt,"lib/mathjax-bundle":et,"lib/rich-editor-extension":$t}
export{oe as default}

//# sourceMappingURL=../../map/plugins/discourse-math_main.Dgzavx-2cm4rkln.digested.js.map
