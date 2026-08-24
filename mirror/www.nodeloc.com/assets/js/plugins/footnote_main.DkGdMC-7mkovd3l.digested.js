const{default:e}=window.moduleBroker.lookup("@glimmer/component"),{on:t}=window.moduleBroker.lookup("@ember/modifier"),{action:o}=window.moduleBroker.lookup("@ember/object"),{trustHTML:n}=window.moduleBroker.lookup("@ember/template"),{default:i}=window.moduleBroker.lookup("discourse/float-kit/components/d-tooltip"),{_INTERNAL_SOURCE_KEY:r,apiInitializer:s}=window.moduleBroker.lookup("discourse/lib/api"),{setComponentTemplate:l}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:a}=window.moduleBroker.lookup("@ember/template-factory"),{withPluginApi:c}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{i18n:d}=window.moduleBroker.lookup("discourse-i18n"),u=Object.freeze({type:"plugin",name:"footnote"})
class f extends e{preventDefault(e){e.preventDefault()}static{dt7948.n(this.prototype,"preventDefault",[o])}static{l(a({id:null,block:'[[[8,[32,0],null,[["@identifier","@interactive","@closeOnScroll","@closeOnClickOutside"],["inline-footnote",true,false,true]],[["trigger","content"],[[[[1,"\\n"],[1,"    "],[11,3],[24,0,"expand-footnote"],[24,6,""],[24,"role","button"],[16,"data-footnote-id",[30,1,["footnoteId"]]],[16,"data-footnote-content",[30,1,["footnoteContent"]]],[4,[32,1],["click",[30,0,["preventDefault"]]],null],[12],[13],[1,"\\n  "]],[]],[[[1,"\\n    "],[1,[28,[32,2],[[30,1,["footnoteContent"]]],null]],[1,"\\n  "]],[]]]]]],["@data"],[]]',moduleName:"(unknown template module)",scope:()=>[i,t,n],isStrictMode:!0}),this)}}var p=function(...e){const t="string"==typeof e[0]?2:1
return e[t]={...e[t],[r]:u},s(...e)}(e=>{e.decorateCookedElement((t,o)=>{if(!e.container.lookup("service:site-settings").display_footnotes_inline)return
const n=t.querySelectorAll("sup.footnote-ref")
n.forEach(e=>{const n=e.querySelector("a")
if(!n)return
const i=n.getAttribute("href"),r=t.querySelector(i)?.cloneNode(!0)
r?.querySelectorAll("sup.footnote-ref, .footnote-backref").forEach(e=>e.remove())
const s=r?.innerHTML,l=document.createElement("span")
l.className="inline-footnote",e.replaceWith(l),o.renderGlimmer(l,f,{footnoteId:i,footnoteContent:s})}),n.length&&t.classList.add("inline-footnotes")})}),m=Object.freeze({__proto__:null,default:p})
const h=Object.freeze({type:"plugin",name:"footnote"})
function w(...e){const t="string"==typeof e[0]?2:1
return e[t]={...e[t],[r]:h},c(...e)}var g={name:"footnote-admin-plugin-configuration-nav",initialize(e){const t=e.lookup("service:current-user")
t?.admin&&w(e=>{e.setAdminPluginIcon("footnote","asterisk")})}},k=Object.freeze({__proto__:null,default:g})
const _={nodeViews:{footnote:function({pmView:{EditorView:e},pmState:{EditorState:t},pmTransform:{StepMap:o}}){return class{constructor(e,t,o){this.node=e,this.outerView=t,this.getPos=o,this.dom=document.createElement("div"),this.dom.className="footnote",this.innerView=null}selectNode(){this.dom.classList.add("ProseMirror-selectednode"),this.innerView||this.open(),this.innerView&&this.innerView.dom.focus()}deselectNode(){this.dom.classList.remove("ProseMirror-selectednode"),this.innerView&&this.close()}open(){const o=this.dom.appendChild(document.createElement("div"))
o.style.setProperty("--footnote-counter",`"${this.#e()}"`),o.className="footnote-tooltip",this.innerView=new e(o,{state:t.create({doc:this.node,plugins:this.outerView.state.plugins.filter(e=>!/^(placeholder|trailing-paragraph)\$.*/.test(e.key))}),dispatchTransaction:this.dispatchInner.bind(this),handleDOMEvents:{mousedown:()=>{this.outerView.hasFocus()&&this.innerView.focus()}}})}#e(){const e=this.dom.closest(".ProseMirror")?.querySelectorAll(".footnote")
return Array.from(e).indexOf(this.dom)+1}close(){this.innerView.destroy(),this.innerView=null,this.dom.textContent=""}dispatchInner(e){const{state:t,transactions:n}=this.innerView.state.applyTransaction(e)
if(this.innerView.updateState(t),!e.getMeta("fromOutside")){const e=this.outerView.state.tr,t=o.offset(this.getPos()+1)
for(let o=0;o<n.length;o++){const i=n[o].steps
for(let o=0;o<i.length;o++)e.step(i[o].map(t))}e.docChanged&&this.outerView.dispatch(e)}}update(e){if(!e.sameMarkup(this.node))return!1
if(this.node=e,this.innerView){const t=this.innerView.state,o=e.content.findDiffStart(t.doc.content)
if(null!=o){let{a:n,b:i}=e.content.findDiffEnd(t.doc.content),r=o-Math.min(n,i)
r>0&&(n+=r,i+=r),this.innerView.dispatch(t.tr.replace(o,i,e.slice(o,n)).setMeta("fromOutside",!0))}}return!0}destroy(){this.innerView&&this.close()}stopEvent(e){return this.innerView&&this.innerView.dom.contains(e.target)}ignoreMutation(){return!0}}}},nodeSpec:{footnote:{attrs:{id:{}},group:"inline",content:"block*",inline:!0,atom:!0,draggable:!1,parseDOM:[{tag:"div.footnote"}],toDOM:()=>["div",{class:"footnote"},0]}},parse:({pmModel:{Slice:e,Fragment:t}})=>({footnote_ref:{node:"footnote",getAttrs:e=>({id:e.meta.id})},footnote_block:{ignore:!0},footnote_open(o,n,i,r){const s=o.top(),l=n.meta.id
let a=i.slice(r+1,i.length-1)
const c=a.findIndex(e=>"footnote_close"===e.type)
a=a.slice(0,c),s.content.forEach((n,i)=>{const r=[]
n.descendants((n,i)=>{if("footnote"!==n.type.name||n.attrs.id!==l)return
o.stack=[],o.openNode(o.schema.nodes.footnote),o.parseTokens(a)
const c=o.closeNode()
o.stack=[s]
const d=new e(t.from(c),0,0)
r.push({from:i,to:i+2,slice:d})})
for(const{from:e,to:t,slice:o}of r)s.content[i]=s.content[i].replace(e,t,o)}),i.splice(r+1,a.length+1)},footnote_anchor:{ignore:!0,noCloseToken:!0}}),serializeNode:{footnote(e,t){if(1===t.content.content.length&&"paragraph"===t.content.firstChild.type.name)e.write("^["),e.renderContent(t.content.firstChild),e.write("]")
else{const o=e.footnoteContents??=[]
o.push(t.content),e.write(`[^${o.length}]`)}},afterSerialize(e){const t=e.footnoteContents
if(t)for(let o=0;o<t.length;o++){const n=e.delim
e.write(`[^${o+1}]: `),e.delim+="    ",e.renderContent(t[o]),e.delim=n}}},inputRules:({pmState:{NodeSelection:e}})=>[{match:/\^\[(.*?)]$/,handler:(e,t,o,n)=>{const i=e.doc.slice(o+2,n).content,r=e.schema.nodes.paragraph.create(null,i),s=e.schema.nodes.footnote.create(null,r)
return e.tr.replaceWith(o,n,s)}},{match:/\[\^\d+]$/,handler:(t,o,n,i)=>{const r=t.schema.nodes.paragraph.create(),s=t.schema.nodes.footnote.create(null,r),l=t.tr.replaceWith(n,i,s)
return l.setSelection(e.create(l.doc,n)),l}}]}
var b=Object.freeze({__proto__:null,default:_}),V={name:"footnotes-composer",initialize(){w(e=>{e.registerRichEditorExtension(_),e.addComposerToolbarPopupMenuOption({action(e){e.addText(`^[${d("footnote.title")}]`)},group:"insertions",icon:"asterisk",label:"footnote.add"})})}}
const y={"api-initializers/inline-footnotes":m,"discourse/initializers/footnote-admin-plugin-configuration-nav":k,"initializers/composer":Object.freeze({__proto__:null,default:V}),"lib/discourse-markdown/footnotes":Object.freeze({__proto__:null,setup:function(e){e.registerOptions((e,t)=>{e.features.footnotes=window.markdownitFootnote&&!!t.enable_markdown_footnotes}),e.allowList(["ol.footnotes-list","hr.footnotes-sep","li.footnote-item","a.footnote-backref","sup.footnote-ref"]),e.allowList({custom(e,t,o){if(("a"===e||"li"===e)&&"id"===t)return!!o.match(/^fn(ref)?\d+$/)}}),window.markdownitFootnote&&e.registerPlugin(window.markdownitFootnote)}}),"lib/rich-editor-extension":b}
export{y as default}

//# sourceMappingURL=../../map/plugins/footnote_main.DkGdMC-7mkovd3l.digested.js.map
