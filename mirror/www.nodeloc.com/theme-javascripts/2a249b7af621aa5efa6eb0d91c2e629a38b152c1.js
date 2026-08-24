const{getObjectForTheme:e}=window.moduleBroker.lookup("discourse/lib/theme-settings-store"),{_INTERNAL_SOURCE_KEY:r,apiInitializer:t}=window.moduleBroker.lookup("discourse/lib/api"),{default:n}=window.moduleBroker.lookup("discourse/lib/get-url"),{i18n:i}=window.moduleBroker.lookup("discourse-i18n"),{default:s}=window.moduleBroker.lookup("@glimmer/component"),{default:o}=window.moduleBroker.lookup("discourse/ui-kit/helpers/d-icon"),{setComponentTemplate:l}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:u}=window.moduleBroker.lookup("@ember/template-factory"),a=e(54)
function c(e){return`theme_translations.54.${e}`}const d=Object.freeze({type:"theme",id:54})
const k="resources"
function _(e){if(!e)return""
const r=c(`js.${e}`),t=i(r)
return!t||t.startsWith("[")?e:t}var p=function(...e){const n="string"==typeof e[0]?2:1
return e[n]={...e[n],[r]:d},t(...e)}(e=>{const r=e.getCurrentUser(),t=a,i=t.resources_links||[]
if(!function(e,r){return"logged_in"===r?Boolean(e):"staff_only"!==r||Boolean(e?.staff)}(r,t.resources_visibility))return
if(!Array.isArray(i)||0===i.length)return
const s=t.resources_section_title_key
e.addSidebarSection((e,r)=>{class t extends r{constructor(e,r){super(),this._link=e,this._index=r}get name(){return`${k}-${this._index}-${this._link.label_key}`}get title(){return _(this._link.label_key)}get text(){return this.title}get href(){return e=this._link.url,/^https?:\/\//i.test(e)?this._link.url:n(this._link.url)
var e}get prefixType(){return"icon"}get prefixValue(){return this._link.icon||"link"}get classNames(){const e=["resources-sidebar__link"]
return this._link.divider_above&&e.push("resources-sidebar__link--with-divider"),e.join(" ")}}return class extends e{get name(){return k}get title(){return _(s)}get text(){return this.title}get displaySection(){return i.length>0}get collapsedByDefault(){return!1}get links(){return i.map((e,r)=>new t(e,r))}}})}),b=Object.freeze({__proto__:null,default:p})
function h(e){if(!e)return""
const r=c(`js.${e}`),t=i(r)
return!t||t.startsWith("[")?e:t}class m extends s{static shouldRender(e,r,t){if(t.lookup("service:current-user"))return!1
const n=a?.resources_visibility
return"everyone"===n}hrefFor=e=>{return r=e.url,/^https?:\/\//i.test(r)?e.url:n(e.url)
var r}
classFor=e=>{const r=["sidebar-section-link","sidebar-row","resources-sidebar__link"]
return e.divider_above&&r.push("resources-sidebar__link--with-divider"),r.join(" ")}
textFor=e=>h(e.label_key)
iconFor=e=>e.icon||"link"
get links(){return Array.isArray(a?.resources_links)?a.resources_links:[]}get titleText(){return h(a?.resources_section_title_key)}static{l(u({id:null,block:'[[[41,[30,0,["links","length"]],[[[1,"  "],[10,0],[15,"data-section-name",[32,0]],[14,0,"sidebar-section sidebar-section-wrapper sidebar-section--expanded"],[12],[1,"\\n    "],[10,0],[14,0,"sidebar-section-header-wrapper sidebar-row"],[12],[1,"\\n      "],[10,1],[14,0,"sidebar-section-header sidebar-section-header-collapsable"],[12],[1,"\\n        "],[10,1],[14,0,"sidebar-section-header-text"],[12],[1,[30,0,["titleText"]]],[13],[1,"\\n      "],[13],[1,"\\n    "],[13],[1,"\\n\\n    "],[10,"ul"],[14,0,"sidebar-section-content"],[12],[1,"\\n"],[42,[28,[31,2],[[28,[31,2],[[30,0,["links"]]],null]],null],null,[[[1,"        "],[10,"li"],[15,"data-list-item-name",[29,[[32,0],"-",[30,2],"-",[30,1,["label_key"]]]]],[14,0,"sidebar-section-link-wrapper"],[12],[1,"\\n          "],[10,3],[15,6,[28,[30,0,["hrefFor"]],[[30,1]],null]],[15,"title",[28,[30,0,["textFor"]],[[30,1]],null]],[15,0,[28,[30,0,["classFor"]],[[30,1]],null]],[12],[1,"\\n            "],[10,1],[14,0,"sidebar-section-link-prefix icon"],[12],[1,"\\n              "],[1,[28,[32,1],[[28,[30,0,["iconFor"]],[[30,1]],null]],null]],[1,"\\n            "],[13],[1,"\\n            "],[10,1],[14,0,"sidebar-section-link-content-text"],[12],[1,"\\n              "],[1,[28,[30,0,["textFor"]],[[30,1]],null]],[1,"\\n            "],[13],[1,"\\n          "],[13],[1,"\\n        "],[13],[1,"\\n"]],[1,2]],null],[1,"    "],[13],[1,"\\n  "],[13],[1,"\\n"]],[]],null]],["link","index"],["if","each","-track-array"]]',moduleName:"(unknown template module)",scope:()=>["resources",o],isStrictMode:!0}),this)}}const f={"discourse/api-initializers/resources-sidebar":b,"discourse/connectors/below-custom-sidebar-sections/resources-anon":Object.freeze({__proto__:null,default:m})}
export{f as default}

//# sourceMappingURL=2a249b7af621aa5efa6eb0d91c2e629a38b152c1.map?__ws=www.nodeloc.com
