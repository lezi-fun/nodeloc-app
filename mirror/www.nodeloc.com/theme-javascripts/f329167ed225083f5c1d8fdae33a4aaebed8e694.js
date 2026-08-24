const{getObjectForTheme:t}=window.moduleBroker.lookup("discourse/lib/theme-settings-store"),{action:e,computed:o}=window.moduleBroker.lookup("@ember/object"),{setOwner:n}=window.moduleBroker.lookup("@ember/owner"),{default:l,service:s}=window.moduleBroker.lookup("@ember/service"),{iconHTML:i,convertIconClass:a}=window.moduleBroker.lookup("discourse/lib/icon-library"),{_INTERNAL_SOURCE_KEY:r}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:c}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{i18n:d}=window.moduleBroker.lookup("discourse-i18n"),{default:u}=window.moduleBroker.lookup("@glimmer/component"),{tracked:p}=window.moduleBroker.lookup("@glimmer/tracking"),{on:h}=window.moduleBroker.lookup("@ember/modifier"),{default:f}=window.moduleBroker.lookup("@ember/render-modifiers/modifiers/did-insert"),{default:m}=window.moduleBroker.lookup("discourse/helpers/concat-class"),{default:g}=window.moduleBroker.lookup("discourse/helpers/d-icon"),{default:y}=window.moduleBroker.lookup("discourse/helpers/noop"),{htmlSafe:b,trustHTML:v}=window.moduleBroker.lookup("@ember/template"),{isEmpty:_}=window.moduleBroker.lookup("@ember/utils"),{isTesting:C}=window.moduleBroker.lookup("discourse/lib/environment"),{default:w}=window.moduleBroker.lookup("discourse/float-kit/components/d-menu"),{fn:T}=window.moduleBroker.lookup("@ember/helper"),{next:S}=window.moduleBroker.lookup("@ember/runloop"),{default:E}=window.moduleBroker.lookup("discourse/components/d-button"),{default:k}=window.moduleBroker.lookup("discourse/components/dropdown-menu"),{default:N}=window.moduleBroker.lookup("discourse/components/text-field"),{default:A}=window.moduleBroker.lookup("discourse/modifiers/scroll-into-view"),{eq:x,or:M,not:O,and:L}=window.moduleBroker.lookup("discourse/truth-helpers"),{setComponentTemplate:D}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:I}=window.moduleBroker.lookup("@ember/template-factory"),{default:z}=window.moduleBroker.lookup("discourse/components/d-toggle-switch"),{default:R}=window.moduleBroker.lookup("@ember/component/template-only"),P=t(53)
function B(t){return`theme_translations.53.${t}`}const U=Object.freeze({type:"theme",id:53})

;/*! @license DOMPurify 3.2.4 | (c) Cure53 and other contributors | Released under the Apache license 2.0 and Mozilla Public License 2.0 | github.com/cure53/DOMPurify/blob/3.2.4/LICENSE */
const{entries:F,setPrototypeOf:H,isFrozen:q,getPrototypeOf:$,getOwnPropertyDescriptor:j}=Object
let{freeze:W,seal:G,create:V}=Object,{apply:Y,construct:X}="undefined"!=typeof Reflect&&Reflect
W||(W=function(t){return t}),G||(G=function(t){return t}),Y||(Y=function(t,e,o){return t.apply(e,o)}),X||(X=function(t,e){return new t(...e)})
const K=ut(Array.prototype.forEach),Z=ut(Array.prototype.lastIndexOf),Q=ut(Array.prototype.pop),J=ut(Array.prototype.push),tt=ut(Array.prototype.splice),et=ut(String.prototype.toLowerCase),ot=ut(String.prototype.toString),nt=ut(String.prototype.match),lt=ut(String.prototype.replace),st=ut(String.prototype.indexOf),it=ut(String.prototype.trim),at=ut(Object.prototype.hasOwnProperty),rt=ut(RegExp.prototype.test),ct=(dt=TypeError,function(){for(var t=arguments.length,e=new Array(t),o=0;o<t;o++)e[o]=arguments[o]
return X(dt,e)})
var dt
function ut(t){return function(e){for(var o=arguments.length,n=new Array(o>1?o-1:0),l=1;l<o;l++)n[l-1]=arguments[l]
return Y(t,e,n)}}function pt(t,e){let o=arguments.length>2&&void 0!==arguments[2]?arguments[2]:et
H&&H(t,null)
let n=e.length
for(;n--;){let l=e[n]
if("string"==typeof l){const t=o(l)
t!==l&&(q(e)||(e[n]=t),l=t)}t[l]=!0}return t}function ht(t){for(let e=0;e<t.length;e++){at(t,e)||(t[e]=null)}return t}function ft(t){const e=V(null)
for(const[o,n]of F(t)){at(t,o)&&(Array.isArray(n)?e[o]=ht(n):n&&"object"==typeof n&&n.constructor===Object?e[o]=ft(n):e[o]=n)}return e}function mt(t,e){for(;null!==t;){const o=j(t,e)
if(o){if(o.get)return ut(o.get)
if("function"==typeof o.value)return ut(o.value)}t=$(t)}return function(){return null}}const gt=W(["a","abbr","acronym","address","area","article","aside","audio","b","bdi","bdo","big","blink","blockquote","body","br","button","canvas","caption","center","cite","code","col","colgroup","content","data","datalist","dd","decorator","del","details","dfn","dialog","dir","div","dl","dt","element","em","fieldset","figcaption","figure","font","footer","form","h1","h2","h3","h4","h5","h6","head","header","hgroup","hr","html","i","img","input","ins","kbd","label","legend","li","main","map","mark","marquee","menu","menuitem","meter","nav","nobr","ol","optgroup","option","output","p","picture","pre","progress","q","rp","rt","ruby","s","samp","section","select","shadow","small","source","spacer","span","strike","strong","style","sub","summary","sup","table","tbody","td","template","textarea","tfoot","th","thead","time","tr","track","tt","u","ul","var","video","wbr"]),yt=W(["svg","a","altglyph","altglyphdef","altglyphitem","animatecolor","animatemotion","animatetransform","circle","clippath","defs","desc","ellipse","filter","font","g","glyph","glyphref","hkern","image","line","lineargradient","marker","mask","metadata","mpath","path","pattern","polygon","polyline","radialgradient","rect","stop","style","switch","symbol","text","textpath","title","tref","tspan","view","vkern"]),bt=W(["feBlend","feColorMatrix","feComponentTransfer","feComposite","feConvolveMatrix","feDiffuseLighting","feDisplacementMap","feDistantLight","feDropShadow","feFlood","feFuncA","feFuncB","feFuncG","feFuncR","feGaussianBlur","feImage","feMerge","feMergeNode","feMorphology","feOffset","fePointLight","feSpecularLighting","feSpotLight","feTile","feTurbulence"]),vt=W(["animate","color-profile","cursor","discard","font-face","font-face-format","font-face-name","font-face-src","font-face-uri","foreignobject","hatch","hatchpath","mesh","meshgradient","meshpatch","meshrow","missing-glyph","script","set","solidcolor","unknown","use"]),_t=W(["math","menclose","merror","mfenced","mfrac","mglyph","mi","mlabeledtr","mmultiscripts","mn","mo","mover","mpadded","mphantom","mroot","mrow","ms","mspace","msqrt","mstyle","msub","msup","msubsup","mtable","mtd","mtext","mtr","munder","munderover","mprescripts"]),Ct=W(["maction","maligngroup","malignmark","mlongdiv","mscarries","mscarry","msgroup","mstack","msline","msrow","semantics","annotation","annotation-xml","mprescripts","none"]),wt=W(["#text"]),Tt=W(["accept","action","align","alt","autocapitalize","autocomplete","autopictureinpicture","autoplay","background","bgcolor","border","capture","cellpadding","cellspacing","checked","cite","class","clear","color","cols","colspan","controls","controlslist","coords","crossorigin","datetime","decoding","default","dir","disabled","disablepictureinpicture","disableremoteplayback","download","draggable","enctype","enterkeyhint","face","for","headers","height","hidden","high","href","hreflang","id","inputmode","integrity","ismap","kind","label","lang","list","loading","loop","low","max","maxlength","media","method","min","minlength","multiple","muted","name","nonce","noshade","novalidate","nowrap","open","optimum","pattern","placeholder","playsinline","popover","popovertarget","popovertargetaction","poster","preload","pubdate","radiogroup","readonly","rel","required","rev","reversed","role","rows","rowspan","spellcheck","scope","selected","shape","size","sizes","span","srclang","start","src","srcset","step","style","summary","tabindex","title","translate","type","usemap","valign","value","width","wrap","xmlns","slot"]),St=W(["accent-height","accumulate","additive","alignment-baseline","amplitude","ascent","attributename","attributetype","azimuth","basefrequency","baseline-shift","begin","bias","by","class","clip","clippathunits","clip-path","clip-rule","color","color-interpolation","color-interpolation-filters","color-profile","color-rendering","cx","cy","d","dx","dy","diffuseconstant","direction","display","divisor","dur","edgemode","elevation","end","exponent","fill","fill-opacity","fill-rule","filter","filterunits","flood-color","flood-opacity","font-family","font-size","font-size-adjust","font-stretch","font-style","font-variant","font-weight","fx","fy","g1","g2","glyph-name","glyphref","gradientunits","gradienttransform","height","href","id","image-rendering","in","in2","intercept","k","k1","k2","k3","k4","kerning","keypoints","keysplines","keytimes","lang","lengthadjust","letter-spacing","kernelmatrix","kernelunitlength","lighting-color","local","marker-end","marker-mid","marker-start","markerheight","markerunits","markerwidth","maskcontentunits","maskunits","max","mask","media","method","mode","min","name","numoctaves","offset","operator","opacity","order","orient","orientation","origin","overflow","paint-order","path","pathlength","patterncontentunits","patterntransform","patternunits","points","preservealpha","preserveaspectratio","primitiveunits","r","rx","ry","radius","refx","refy","repeatcount","repeatdur","restart","result","rotate","scale","seed","shape-rendering","slope","specularconstant","specularexponent","spreadmethod","startoffset","stddeviation","stitchtiles","stop-color","stop-opacity","stroke-dasharray","stroke-dashoffset","stroke-linecap","stroke-linejoin","stroke-miterlimit","stroke-opacity","stroke","stroke-width","style","surfacescale","systemlanguage","tabindex","tablevalues","targetx","targety","transform","transform-origin","text-anchor","text-decoration","text-rendering","textlength","type","u1","u2","unicode","values","viewbox","visibility","version","vert-adv-y","vert-origin-x","vert-origin-y","width","word-spacing","wrap","writing-mode","xchannelselector","ychannelselector","x","x1","x2","xmlns","y","y1","y2","z","zoomandpan"]),Et=W(["accent","accentunder","align","bevelled","close","columnsalign","columnlines","columnspan","denomalign","depth","dir","display","displaystyle","encoding","fence","frame","height","href","id","largeop","length","linethickness","lspace","lquote","mathbackground","mathcolor","mathsize","mathvariant","maxsize","minsize","movablelimits","notation","numalign","open","rowalign","rowlines","rowspacing","rowspan","rspace","rquote","scriptlevel","scriptminsize","scriptsizemultiplier","selection","separator","separators","stretchy","subscriptshift","supscriptshift","symmetric","voffset","width","xmlns"]),kt=W(["xlink:href","xml:id","xlink:title","xml:space","xmlns:xlink"]),Nt=G(/\{\{[\w\W]*|[\w\W]*\}\}/gm),At=G(/<%[\w\W]*|[\w\W]*%>/gm),xt=G(/\$\{[\w\W]*/gm),Mt=G(/^data-[\-\w.\u00B7-\uFFFF]+$/),Ot=G(/^aria-[\-\w]+$/),Lt=G(/^(?:(?:(?:f|ht)tps?|mailto|tel|callto|sms|cid|xmpp):|[^a-z]|[a-z+.\-]+(?:[^a-z+.\-:]|$))/i),Dt=G(/^(?:\w+script|data):/i),It=G(/[\u0000-\u0020\u00A0\u1680\u180E\u2000-\u2029\u205F\u3000]/g),zt=G(/^html$/i),Rt=G(/^[a-z][.\w]*(-[.\w]+)+$/i)
let Pt=Object.freeze({__proto__:null,ARIA_ATTR:Ot,ATTR_WHITESPACE:It,CUSTOM_ELEMENT:Rt,DATA_ATTR:Mt,DOCTYPE_NAME:zt,ERB_EXPR:At,IS_ALLOWED_URI:Lt,IS_SCRIPT_OR_DATA:Dt,MUSTACHE_EXPR:Nt,TMPLIT_EXPR:xt})
const Bt=1,Ut=3,Ft=7,Ht=8,qt=9,$t=function(){return"undefined"==typeof window?null:window}
let jt=function t(){let e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:$t()
const o=e=>t(e)
if(o.version="3.2.4",o.removed=[],!e||!e.document||e.document.nodeType!==qt||!e.Element)return o.isSupported=!1,o
let{document:n}=e
const l=n,s=l.currentScript,{DocumentFragment:i,HTMLTemplateElement:a,Node:r,Element:c,NodeFilter:d,NamedNodeMap:u=e.NamedNodeMap||e.MozNamedAttrMap,HTMLFormElement:p,DOMParser:h,trustedTypes:f}=e,m=c.prototype,g=mt(m,"cloneNode"),y=mt(m,"remove"),b=mt(m,"nextSibling"),v=mt(m,"childNodes"),_=mt(m,"parentNode")
if("function"==typeof a){const t=n.createElement("template")
t.content&&t.content.ownerDocument&&(n=t.content.ownerDocument)}let C,w=""
const{implementation:T,createNodeIterator:S,createDocumentFragment:E,getElementsByTagName:k}=n,{importNode:N}=l
let A={afterSanitizeAttributes:[],afterSanitizeElements:[],afterSanitizeShadowDOM:[],beforeSanitizeAttributes:[],beforeSanitizeElements:[],beforeSanitizeShadowDOM:[],uponSanitizeAttribute:[],uponSanitizeElement:[],uponSanitizeShadowNode:[]}
o.isSupported="function"==typeof F&&"function"==typeof _&&T&&void 0!==T.createHTMLDocument
const{MUSTACHE_EXPR:x,ERB_EXPR:M,TMPLIT_EXPR:O,DATA_ATTR:L,ARIA_ATTR:D,IS_SCRIPT_OR_DATA:I,ATTR_WHITESPACE:z,CUSTOM_ELEMENT:R}=Pt
let{IS_ALLOWED_URI:P}=Pt,B=null
const U=pt({},[...gt,...yt,...bt,..._t,...wt])
let H=null
const q=pt({},[...Tt,...St,...Et,...kt])
let $=Object.seal(V(null,{tagNameCheck:{writable:!0,configurable:!1,enumerable:!0,value:null},attributeNameCheck:{writable:!0,configurable:!1,enumerable:!0,value:null},allowCustomizedBuiltInElements:{writable:!0,configurable:!1,enumerable:!0,value:!1}})),j=null,G=null,Y=!0,X=!0,dt=!1,ut=!0,ht=!1,Nt=!0,At=!1,xt=!1,Mt=!1,Ot=!1,Dt=!1,It=!1,Rt=!0,jt=!1,Wt=!0,Gt=!1,Vt={},Yt=null
const Xt=pt({},["annotation-xml","audio","colgroup","desc","foreignobject","head","iframe","math","mi","mn","mo","ms","mtext","noembed","noframes","noscript","plaintext","script","style","svg","template","thead","title","video","xmp"])
let Kt=null
const Zt=pt({},["audio","video","img","source","image","track"])
let Qt=null
const Jt=pt({},["alt","class","for","id","label","name","pattern","placeholder","role","summary","title","value","style","xmlns"]),te="http://www.w3.org/1998/Math/MathML",ee="http://www.w3.org/2000/svg",oe="http://www.w3.org/1999/xhtml"
let ne=oe,le=!1,se=null
const ie=pt({},[te,ee,oe],ot)
let ae=pt({},["mi","mo","mn","ms","mtext"]),re=pt({},["annotation-xml"])
const ce=pt({},["title","style","font","a","script"])
let de=null
const ue=["application/xhtml+xml","text/html"]
let pe=null,he=null
const fe=n.createElement("form"),me=function(t){return t instanceof RegExp||t instanceof Function},ge=function(){let t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{}
if(!he||he!==t){if(t&&"object"==typeof t||(t={}),t=ft(t),de=-1===ue.indexOf(t.PARSER_MEDIA_TYPE)?"text/html":t.PARSER_MEDIA_TYPE,pe="application/xhtml+xml"===de?ot:et,B=at(t,"ALLOWED_TAGS")?pt({},t.ALLOWED_TAGS,pe):U,H=at(t,"ALLOWED_ATTR")?pt({},t.ALLOWED_ATTR,pe):q,se=at(t,"ALLOWED_NAMESPACES")?pt({},t.ALLOWED_NAMESPACES,ot):ie,Qt=at(t,"ADD_URI_SAFE_ATTR")?pt(ft(Jt),t.ADD_URI_SAFE_ATTR,pe):Jt,Kt=at(t,"ADD_DATA_URI_TAGS")?pt(ft(Zt),t.ADD_DATA_URI_TAGS,pe):Zt,Yt=at(t,"FORBID_CONTENTS")?pt({},t.FORBID_CONTENTS,pe):Xt,j=at(t,"FORBID_TAGS")?pt({},t.FORBID_TAGS,pe):{},G=at(t,"FORBID_ATTR")?pt({},t.FORBID_ATTR,pe):{},Vt=!!at(t,"USE_PROFILES")&&t.USE_PROFILES,Y=!1!==t.ALLOW_ARIA_ATTR,X=!1!==t.ALLOW_DATA_ATTR,dt=t.ALLOW_UNKNOWN_PROTOCOLS||!1,ut=!1!==t.ALLOW_SELF_CLOSE_IN_ATTR,ht=t.SAFE_FOR_TEMPLATES||!1,Nt=!1!==t.SAFE_FOR_XML,At=t.WHOLE_DOCUMENT||!1,Ot=t.RETURN_DOM||!1,Dt=t.RETURN_DOM_FRAGMENT||!1,It=t.RETURN_TRUSTED_TYPE||!1,Mt=t.FORCE_BODY||!1,Rt=!1!==t.SANITIZE_DOM,jt=t.SANITIZE_NAMED_PROPS||!1,Wt=!1!==t.KEEP_CONTENT,Gt=t.IN_PLACE||!1,P=t.ALLOWED_URI_REGEXP||Lt,ne=t.NAMESPACE||oe,ae=t.MATHML_TEXT_INTEGRATION_POINTS||ae,re=t.HTML_INTEGRATION_POINTS||re,$=t.CUSTOM_ELEMENT_HANDLING||{},t.CUSTOM_ELEMENT_HANDLING&&me(t.CUSTOM_ELEMENT_HANDLING.tagNameCheck)&&($.tagNameCheck=t.CUSTOM_ELEMENT_HANDLING.tagNameCheck),t.CUSTOM_ELEMENT_HANDLING&&me(t.CUSTOM_ELEMENT_HANDLING.attributeNameCheck)&&($.attributeNameCheck=t.CUSTOM_ELEMENT_HANDLING.attributeNameCheck),t.CUSTOM_ELEMENT_HANDLING&&"boolean"==typeof t.CUSTOM_ELEMENT_HANDLING.allowCustomizedBuiltInElements&&($.allowCustomizedBuiltInElements=t.CUSTOM_ELEMENT_HANDLING.allowCustomizedBuiltInElements),ht&&(X=!1),Dt&&(Ot=!0),Vt&&(B=pt({},wt),H=[],!0===Vt.html&&(pt(B,gt),pt(H,Tt)),!0===Vt.svg&&(pt(B,yt),pt(H,St),pt(H,kt)),!0===Vt.svgFilters&&(pt(B,bt),pt(H,St),pt(H,kt)),!0===Vt.mathMl&&(pt(B,_t),pt(H,Et),pt(H,kt))),t.ADD_TAGS&&(B===U&&(B=ft(B)),pt(B,t.ADD_TAGS,pe)),t.ADD_ATTR&&(H===q&&(H=ft(H)),pt(H,t.ADD_ATTR,pe)),t.ADD_URI_SAFE_ATTR&&pt(Qt,t.ADD_URI_SAFE_ATTR,pe),t.FORBID_CONTENTS&&(Yt===Xt&&(Yt=ft(Yt)),pt(Yt,t.FORBID_CONTENTS,pe)),Wt&&(B["#text"]=!0),At&&pt(B,["html","head","body"]),B.table&&(pt(B,["tbody"]),delete j.tbody),t.TRUSTED_TYPES_POLICY){if("function"!=typeof t.TRUSTED_TYPES_POLICY.createHTML)throw ct('TRUSTED_TYPES_POLICY configuration option must provide a "createHTML" hook.')
if("function"!=typeof t.TRUSTED_TYPES_POLICY.createScriptURL)throw ct('TRUSTED_TYPES_POLICY configuration option must provide a "createScriptURL" hook.')
C=t.TRUSTED_TYPES_POLICY,w=C.createHTML("")}else void 0===C&&(C=function(t,e){if("object"!=typeof t||"function"!=typeof t.createPolicy)return null
let o=null
const n="data-tt-policy-suffix"
e&&e.hasAttribute(n)&&(o=e.getAttribute(n))
const l="dompurify"+(o?"#"+o:"")
try{return t.createPolicy(l,{createHTML:t=>t,createScriptURL:t=>t})}catch(t){return console.warn("TrustedTypes policy "+l+" could not be created."),null}}(f,s)),null!==C&&"string"==typeof w&&(w=C.createHTML(""))
W&&W(t),he=t}},ye=pt({},[...yt,...bt,...vt]),be=pt({},[..._t,...Ct]),ve=function(t){J(o.removed,{element:t})
try{_(t).removeChild(t)}catch(e){y(t)}},_e=function(t,e){try{J(o.removed,{attribute:e.getAttributeNode(t),from:e})}catch(t){J(o.removed,{attribute:null,from:e})}if(e.removeAttribute(t),"is"===t)if(Ot||Dt)try{ve(e)}catch(t){}else try{e.setAttribute(t,"")}catch(t){}},Ce=function(t){let e=null,o=null
if(Mt)t="<remove></remove>"+t
else{const e=nt(t,/^[\r\n\t ]+/)
o=e&&e[0]}"application/xhtml+xml"===de&&ne===oe&&(t='<html xmlns="http://www.w3.org/1999/xhtml"><head></head><body>'+t+"</body></html>")
const l=C?C.createHTML(t):t
if(ne===oe)try{e=(new h).parseFromString(l,de)}catch(t){}if(!e||!e.documentElement){e=T.createDocument(ne,"template",null)
try{e.documentElement.innerHTML=le?w:l}catch(t){}}const s=e.body||e.documentElement
return t&&o&&s.insertBefore(n.createTextNode(o),s.childNodes[0]||null),ne===oe?k.call(e,At?"html":"body")[0]:At?e.documentElement:s},we=function(t){return S.call(t.ownerDocument||t,t,d.SHOW_ELEMENT|d.SHOW_COMMENT|d.SHOW_TEXT|d.SHOW_PROCESSING_INSTRUCTION|d.SHOW_CDATA_SECTION,null)},Te=function(t){return t instanceof p&&("string"!=typeof t.nodeName||"string"!=typeof t.textContent||"function"!=typeof t.removeChild||!(t.attributes instanceof u)||"function"!=typeof t.removeAttribute||"function"!=typeof t.setAttribute||"string"!=typeof t.namespaceURI||"function"!=typeof t.insertBefore||"function"!=typeof t.hasChildNodes)},Se=function(t){return"function"==typeof r&&t instanceof r}
function Ee(t,e,n){K(t,t=>{t.call(o,e,n,he)})}const ke=function(t){let e=null
if(Ee(A.beforeSanitizeElements,t,null),Te(t))return ve(t),!0
const n=pe(t.nodeName)
if(Ee(A.uponSanitizeElement,t,{tagName:n,allowedTags:B}),t.hasChildNodes()&&!Se(t.firstElementChild)&&rt(/<[/\w]/g,t.innerHTML)&&rt(/<[/\w]/g,t.textContent))return ve(t),!0
if(t.nodeType===Ft)return ve(t),!0
if(Nt&&t.nodeType===Ht&&rt(/<[/\w]/g,t.data))return ve(t),!0
if(!B[n]||j[n]){if(!j[n]&&Ae(n)){if($.tagNameCheck instanceof RegExp&&rt($.tagNameCheck,n))return!1
if($.tagNameCheck instanceof Function&&$.tagNameCheck(n))return!1}if(Wt&&!Yt[n]){const e=_(t)||t.parentNode,o=v(t)||t.childNodes
if(o&&e){for(let n=o.length-1;n>=0;--n){const l=g(o[n],!0)
l.__removalCount=(t.__removalCount||0)+1,e.insertBefore(l,b(t))}}}return ve(t),!0}return t instanceof c&&!function(t){let e=_(t)
e&&e.tagName||(e={namespaceURI:ne,tagName:"template"})
const o=et(t.tagName),n=et(e.tagName)
return!!se[t.namespaceURI]&&(t.namespaceURI===ee?e.namespaceURI===oe?"svg"===o:e.namespaceURI===te?"svg"===o&&("annotation-xml"===n||ae[n]):Boolean(ye[o]):t.namespaceURI===te?e.namespaceURI===oe?"math"===o:e.namespaceURI===ee?"math"===o&&re[n]:Boolean(be[o]):t.namespaceURI===oe?!(e.namespaceURI===ee&&!re[n])&&!(e.namespaceURI===te&&!ae[n])&&!be[o]&&(ce[o]||!ye[o]):!("application/xhtml+xml"!==de||!se[t.namespaceURI]))}(t)?(ve(t),!0):"noscript"!==n&&"noembed"!==n&&"noframes"!==n||!rt(/<\/no(script|embed|frames)/i,t.innerHTML)?(ht&&t.nodeType===Ut&&(e=t.textContent,K([x,M,O],t=>{e=lt(e,t," ")}),t.textContent!==e&&(J(o.removed,{element:t.cloneNode()}),t.textContent=e)),Ee(A.afterSanitizeElements,t,null),!1):(ve(t),!0)},Ne=function(t,e,o){if(Rt&&("id"===e||"name"===e)&&(o in n||o in fe))return!1
if(X&&!G[e]&&rt(L,e));else if(Y&&rt(D,e));else if(!H[e]||G[e]){if(!(Ae(t)&&($.tagNameCheck instanceof RegExp&&rt($.tagNameCheck,t)||$.tagNameCheck instanceof Function&&$.tagNameCheck(t))&&($.attributeNameCheck instanceof RegExp&&rt($.attributeNameCheck,e)||$.attributeNameCheck instanceof Function&&$.attributeNameCheck(e))||"is"===e&&$.allowCustomizedBuiltInElements&&($.tagNameCheck instanceof RegExp&&rt($.tagNameCheck,o)||$.tagNameCheck instanceof Function&&$.tagNameCheck(o))))return!1}else if(Qt[e]);else if(rt(P,lt(o,z,"")));else if("src"!==e&&"xlink:href"!==e&&"href"!==e||"script"===t||0!==st(o,"data:")||!Kt[t]){if(dt&&!rt(I,lt(o,z,"")));else if(o)return!1}else;return!0},Ae=function(t){return"annotation-xml"!==t&&nt(t,R)},xe=function(t){Ee(A.beforeSanitizeAttributes,t,null)
const{attributes:e}=t
if(!e||Te(t))return
const n={attrName:"",attrValue:"",keepAttr:!0,allowedAttributes:H,forceKeepAttr:void 0}
let l=e.length
for(;l--;){const s=e[l],{name:i,namespaceURI:a,value:r}=s,c=pe(i)
let d="value"===i?r:it(r)
if(n.attrName=c,n.attrValue=d,n.keepAttr=!0,n.forceKeepAttr=void 0,Ee(A.uponSanitizeAttribute,t,n),d=n.attrValue,!jt||"id"!==c&&"name"!==c||(_e(i,t),d="user-content-"+d),Nt&&rt(/((--!?|])>)|<\/(style|title)/i,d)){_e(i,t)
continue}if(n.forceKeepAttr)continue
if(_e(i,t),!n.keepAttr)continue
if(!ut&&rt(/\/>/i,d)){_e(i,t)
continue}ht&&K([x,M,O],t=>{d=lt(d,t," ")})
const u=pe(t.nodeName)
if(Ne(u,c,d)){if(C&&"object"==typeof f&&"function"==typeof f.getAttributeType)if(a);else switch(f.getAttributeType(u,c)){case"TrustedHTML":d=C.createHTML(d)
break
case"TrustedScriptURL":d=C.createScriptURL(d)}try{a?t.setAttributeNS(a,i,d):t.setAttribute(i,d),Te(t)?ve(t):Q(o.removed)}catch(t){}}}Ee(A.afterSanitizeAttributes,t,null)},Me=function t(e){let o=null
const n=we(e)
for(Ee(A.beforeSanitizeShadowDOM,e,null);o=n.nextNode();)Ee(A.uponSanitizeShadowNode,o,null),ke(o),xe(o),o.content instanceof i&&t(o.content)
Ee(A.afterSanitizeShadowDOM,e,null)}
return o.sanitize=function(t){let e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=null,s=null,a=null,c=null
if(le=!t,le&&(t="\x3c!--\x3e"),"string"!=typeof t&&!Se(t)){if("function"!=typeof t.toString)throw ct("toString is not a function")
if("string"!=typeof(t=t.toString()))throw ct("dirty is not a string, aborting")}if(!o.isSupported)return t
if(xt||ge(e),o.removed=[],"string"==typeof t&&(Gt=!1),Gt){if(t.nodeName){const e=pe(t.nodeName)
if(!B[e]||j[e])throw ct("root node is forbidden and cannot be sanitized in-place")}}else if(t instanceof r)n=Ce("\x3c!----\x3e"),s=n.ownerDocument.importNode(t,!0),s.nodeType===Bt&&"BODY"===s.nodeName||"HTML"===s.nodeName?n=s:n.appendChild(s)
else{if(!Ot&&!ht&&!At&&-1===t.indexOf("<"))return C&&It?C.createHTML(t):t
if(n=Ce(t),!n)return Ot?null:It?w:""}n&&Mt&&ve(n.firstChild)
const d=we(Gt?t:n)
for(;a=d.nextNode();)ke(a),xe(a),a.content instanceof i&&Me(a.content)
if(Gt)return t
if(Ot){if(Dt)for(c=E.call(n.ownerDocument);n.firstChild;)c.appendChild(n.firstChild)
else c=n
return(H.shadowroot||H.shadowrootmode)&&(c=N.call(l,c,!0)),c}let u=At?n.outerHTML:n.innerHTML
return At&&B["!doctype"]&&n.ownerDocument&&n.ownerDocument.doctype&&n.ownerDocument.doctype.name&&rt(zt,n.ownerDocument.doctype.name)&&(u="<!DOCTYPE "+n.ownerDocument.doctype.name+">\n"+u),ht&&K([x,M,O],t=>{u=lt(u,t," ")}),C&&It?C.createHTML(u):u},o.setConfig=function(){ge(arguments.length>0&&void 0!==arguments[0]?arguments[0]:{}),xt=!0},o.clearConfig=function(){he=null,xt=!1},o.isValidAttribute=function(t,e,o){he||ge({})
const n=pe(t),l=pe(e)
return Ne(n,l,o)},o.addHook=function(t,e){"function"==typeof e&&J(A[t],e)},o.removeHook=function(t,e){if(void 0!==e){const o=Z(A[t],e)
return-1===o?void 0:tt(A[t],o,1)[0]}return Q(A[t])},o.removeHooks=function(t){A[t]=[]},o.removeAllHooks=function(){A={afterSanitizeAttributes:[],afterSanitizeElements:[],afterSanitizeShadowDOM:[],beforeSanitizeAttributes:[],beforeSanitizeElements:[],beforeSanitizeShadowDOM:[],uponSanitizeAttribute:[],uponSanitizeElement:[],uponSanitizeShadowNode:[]}},o}()
var Wt=Object.freeze({__proto__:null,default:jt})
const Gt={RETURN_DOM_FRAGMENT:!0,ALLOWED_TAGS:["svg","path","circle","rect","line","polyline","polygon","ellipse","g","defs","title","linearGradient","radialGradient","stop","mask","pattern","clipPath"],ALLOWED_ATTR:["viewBox","d","points","preserveAspectRatio","fill","stroke","stroke-width","stroke-linecap","stroke-linejoin","stroke-dasharray","stroke-dashoffset","stroke-opacity","fill-opacity","opacity","transform","transform-origin","cx","cy","r","rx","ry","x","y","x1","y1","x2","y2","width","height","gradientUnits","gradientTransform","offset","stop-color","stop-opacity","id","class","style","patternUnits","maskUnits","maskContentUnits"],USE_PROFILES:{svg:!0},ALLOW_DATA_ATTR:!1,ALLOW_UNKNOWN_PROTOCOLS:!1,ALLOW_NAMESPACES:!1}
function Vt(t,e=16){const o=jt.sanitize(t,Gt),n=document.importNode(o,!0).firstChild
return n&&n instanceof SVGSVGElement?(n.setAttribute("width",`${e}`),n.setAttribute("height",`${e}`),n.outerHTML):""}var Yt=Object.freeze({__proto__:null,createSafeSVG:Vt})
function Xt(t){return _(t)?"":t.startsWith("<svg")?b(Vt(t)):b(i(a(t)))}var Kt=Object.freeze({__proto__:null,default:Xt})
function Zt(t,e,o){if(t){if(t.classList.add("is-clipped"),CSS.supports("interpolate-size: allow-keywords"))return t.classList.add("can-interpolate-size"),o(e),void(e||t.addEventListener("transitionend",()=>t.classList.remove("is-clipped"),{once:!0}))
t.removeAttribute("style"),t.style.height=t.scrollHeight+"px",e&&(t.style.height=t.scrollHeight+"px",t.offsetHeight,t.style.height="0px"),o(e),t.addEventListener("transitionend",()=>{e?t.style.display="none":(t.style.height="",t.classList.remove("is-clipped"))},{once:!0})}}function Qt(t,e){let o=t.replace("#","")
3===o.length&&(o=o[0]+o[0]+o[1]+o[1]+o[2]+o[2])
return`rgba(${parseInt(o.substring(0,2),16)},${parseInt(o.substring(2,4),16)},${parseInt(o.substring(4,6),16)}, ${e})`}function Jt(t){return t.charAt(0).toUpperCase()+t.slice(1)}function te(t){const e=!t.textContent.trim(),o=!t.children.length,n=Array.from(t.childNodes).filter(t=>t.nodeType===Node.TEXT_NODE).every(t=>!t.textContent.trim())
return e&&o&&n}function ee(t,e,{onEach:o,onStop:n,onEnd:l}={}){const s=[]
for(const l of Array.from(t.childNodes)){if(e(l)){n?.(l)
break}s.push(l),o?.(l)}return l?.(s),s}function oe(t){let e=t.firstChild
for(;e&&e.nodeType===Node.TEXT_NODE&&!e.textContent.trim();)e=e.nextSibling
return e}function ne(t){if(!t)return null
if(t.nodeType===Node.TEXT_NODE)return t.textContent
if(t.nodeType===Node.ELEMENT_NODE){const e=oe(t)
if(e?.nodeType===Node.TEXT_NODE)return e.textContent}return null}var le=Object.freeze({__proto__:null,capitalizeFirstLetter:Jt,collectNodesUntil:ee,firstMeaningfulNode:oe,hexToRGBA:Qt,isNodeEmpty:te,leadingTextFromNode:ne,toggleCalloutCollapse:Zt})
const se=new RegExp("\\[![^\\]]+\\][+-]? *","gmi"),ie=/^(?<marker>\[!(?<callout>[^\]]+)\](?<fold>[+-])?\s*?)(?<title>.*)?/,ae=/\[![^\]]+\]/,re=P.callout_fallback_type||"note"
let ce
function de(){return ce&&!C()||(ce=function(t){const e=[]
for(const o of t){const t=(o.alias??"").split("|").map(t=>t.trim().toLowerCase()).filter(Boolean),n=o.type.trim().toLowerCase(),l=o.title?.trim(),s=Boolean(l)
e.push({...o,type:n,name:n,title:l||Jt(n),aliases:t,hasExplicitTitle:s})
for(const i of t)e.push({...o,type:i,mainType:n,name:i,title:l||Jt(i),hasExplicitTitle:s})}return e}(P.callouts||[])),ce}function ue(t){return de().find(e=>e.type===t?.toLowerCase())}function pe(){return de()}function he(){return de().filter(t=>!t.mainType||!t.hasExplicitTitle)}function fe(){return de().map(t=>t.type)}function me(t){return fe().filter(e=>e.startsWith(t.toLowerCase()))}var ge=Object.freeze({__proto__:null,CALLOUT_CONTROLS_META:"callout:controls",CALLOUT_EXCERPT_REGEX:se,CALLOUT_MARKER_REGEX:ae,CALLOUT_REGEX:ie,DEFAULT_CALLOUT_TYPE:re,findCalloutOptions:ue,getAllCalloutTypes:fe,getAllCallouts:pe,getChooserCallouts:he,searchCallouts:me})
class ye extends u{static{dt7948.g(this.prototype,"calloutSettings",[s])}#t=void dt7948.i(this,"calloutSettings")
static{dt7948.g(this.prototype,"searchTerm",[p],function(){return""})}#e=void dt7948.i(this,"searchTerm")
static{dt7948.g(this.prototype,"selectedTypeIndex",[p],function(){return-1})}#o=void dt7948.i(this,"selectedTypeIndex")
get filteredCallouts(){const t=this.calloutSettings.chooser()
if(!this.searchTerm)return t
const e=this.searchTerm.toLowerCase()
return t.filter(t=>t.type.includes(e)||t.title.toLowerCase().includes(e)||t.hasExplicitTitle&&t.aliases?.some(t=>t.includes(e)))}search(t){this.searchTerm=t.target.value,this.selectedTypeIndex=-1}static{dt7948.n(this.prototype,"search",[e])}handleKeydown(t){const e=this.filteredCallouts
if("ArrowDown"===t.key)t.preventDefault(),this.selectedTypeIndex=Math.min(this.selectedTypeIndex+1,e.length-1)
else if("ArrowUp"===t.key)t.preventDefault(),this.selectedTypeIndex=Math.max(this.selectedTypeIndex-1,0)
else if("Enter"===t.key){t.preventDefault()
const o=e[this.selectedTypeIndex]
o&&this.selectType(o.type)}}static{dt7948.n(this.prototype,"handleKeydown",[e])}focus(t){t.focus({preventScroll:!0}),S(()=>{this.selectedTypeIndex=this.filteredCallouts.findIndex(t=>t.type===this.args.selectedType)})}static{dt7948.n(this.prototype,"focus",[e])}selectType(t){this.args.onSelect?.(t),this.args.close?.()}static{dt7948.n(this.prototype,"selectType",[e])}calloutColorStyle(t){const e=Qt(t||P.callout_fallback_color,P.callout_background_opacity/100)
return v(`--q-callout-background: ${e}; --q-callout-color: ${t||P.callout_fallback_color};`)}static{D(I({id:null,block:'[[[10,0],[14,0,"callout-chooser-panel"],[12],[1,"\\n  "],[10,0],[14,0,"callout-chooser-search"],[12],[1,"\\n    "],[8,[32,0],[[24,"autocomplete","off"],[4,[32,3],["input",[30,0,["search"]]],null],[4,[32,3],["keydown",[30,0,["handleKeydown"]]],null],[4,[32,4],[[30,0,["focus"]]],null]],[["@placeholder","@type","@value"],[[28,[32,1],[[28,[32,2],["composer.menu.search"],null]],null],"search",[28,[31,0],[[30,0,["searchTerm"]]],null]]],null],[1,"\\n    "],[1,[28,[32,5],["magnifying-glass"],null]],[1,"\\n  "],[13],[1,"\\n  "],[8,[32,6],[[24,0,"callout-chooser-list"]],null,[["default"],[[[[1,"\\n"],[42,[28,[31,2],[[28,[31,2],[[30,0,["filteredCallouts"]]],null]],null],null,[[[1,"      "],[8,[30,1,["item"]],null,null,[["default"],[[[[1,"\\n        "],[8,[32,7],[[16,0,[28,[32,8],["callout-chooser-row",[52,[28,[32,9],[[30,2,["type"]],[30,4]],null],"is-selected"],[52,[28,[32,9],[[30,3],[30,0,["selectedTypeIndex"]]],null],"is-highlighted"]],null]],[16,"data-type",[30,2,["type"]]],[16,5,[28,[30,0,["calloutColorStyle"]],[[30,2,["color"]]],null]],[4,[32,11],[[28,[32,9],[[30,3],[30,0,["selectedTypeIndex"]]],null]],null]],[["@action"],[[28,[32,10],[[30,0,["selectType"]],[30,2,["type"]]],null]]],[["default"],[[[[1,"\\n          "],[10,1],[14,0,"callout-chooser-row__icon"],[12],[1,"\\n            "],[1,[28,[32,12],[[30,2,["icon"]]],null]],[1,"\\n          "],[13],[1,"\\n          "],[10,1],[14,0,"callout-chooser-row__name"],[12],[1,"\\n            "],[1,[30,2,["title"]]],[1,"\\n          "],[13],[1,"\\n        "]],[]]]]],[1,"\\n      "]],[]]]]],[1,"\\n"]],[2,3]],null],[1,"  "]],[1]]]]],[1,"\\n"],[13]],["menu","callout","index","@selectedType"],["readonly","each","-track-array","if"]]',moduleName:"(unknown template module)",scope:()=>[N,d,B,h,f,g,k,E,m,x,T,A,Xt],isStrictMode:!0}),this)}}var be=Object.freeze({__proto__:null,default:ye})
class ve extends u{static{dt7948.g(this.prototype,"calloutSettings",[s])}#t=void dt7948.i(this,"calloutSettings")
menuApi=null
get icon(){const t=this.calloutSettings.find(this.args.value)
return t?.icon||P.callout_fallback_icon}onRegisterApi(t){this.menuApi=t}static{dt7948.n(this.prototype,"onRegisterApi",[e])}onClose(){this.args.onClose?.()}static{dt7948.n(this.prototype,"onClose",[e])}onSelect(t){this.menuApi?.close(),this.args.onChange?.(t)}static{dt7948.n(this.prototype,"onSelect",[e])}static{D(I({id:null,block:'[[[8,[32,0],[[17,1]],[["@identifier","@triggerClass","@contentClass","@placement","@disabled","@onClose","@onRegisterApi","@offset"],["callout-chooser","callout-chooser-trigger","callout-chooser-content","bottom-start",[30,2],[30,0,["onClose"]],[30,0,["onRegisterApi"]],2]],[["trigger","content"],[[[[1,"\\n    "],[10,1],[14,0,"callout-icon"],[12],[1,"\\n      "],[1,[28,[32,1],[[30,0,["icon"]]],null]],[1,"\\n    "],[13],[1,"\\n  "]],[]],[[[1,"\\n    "],[8,[32,2],null,[["@selectedType","@onSelect"],[[30,3],[30,0,["onSelect"]]]],null],[1,"\\n  "]],[]]]]]],["&attrs","@disabled","@value"],[]]',moduleName:"(unknown template module)",scope:()=>[w,Xt,ye],isStrictMode:!0}),this)}}var _e=Object.freeze({__proto__:null,default:ve})
class Ce extends u{static{dt7948.g(this.prototype,"calloutSettings",[s])}#t=void dt7948.i(this,"calloutSettings")
static{dt7948.g(this.prototype,"isCollapsed",[p])}#n=void dt7948.i(this,"isCollapsed")
contentElement=null
type=re
icon=P.callout_fallback_icon
alias=this.type
constructor(){super(...arguments)
const{type:t,fold:e}=this.args.data
this.isCollapsed=e&&"-"===e,this.options=this.calloutSettings.find(t),this.options?.type&&(this.type=this.options.mainType||this.options.type,this.alias=t),this.options?.icon&&(this.icon=this.options.icon)}setupContent(t){this.contentElement=t}static{dt7948.n(this.prototype,"setupContent",[e])}setupColor(t){t.style.setProperty("--q-callout-background",Qt(this.options?.color||P.callout_fallback_color,P.callout_background_opacity/100)),t.style.setProperty("--q-callout-color",this.options?.color||P.callout_fallback_color)}static{dt7948.n(this.prototype,"setupColor",[e])}preventSelection(t){t.detail>1&&t.preventDefault()}static{dt7948.n(this.prototype,"preventSelection",[e])}toggleCollapse(){const t=!this.isCollapsed
Zt(this.contentElement,t,t=>{this.isCollapsed=t})}static{dt7948.n(this.prototype,"toggleCollapse",[e])}get isCollapsible(){return["-","+"].includes(this.args.data.fold)&&this.args.data.children?.length>0}get title(){return this.args.data.title.text||this.options?.title||Jt(this.type)}onTypeChange(t){const{calloutIndex:e}=this.args.data,o=document.querySelector(".d-editor-input")
if(!o)return
const n=o.value,l=/\[!([^\]]+)\]/gim
let s,i=0
for(;null!==(s=l.exec(n));){const l=n.lastIndexOf("\n",s.index-1)+1,a=n.substring(l,s.index)
if(/^(?:>[ \t]*)+$/.test(a)){if(i===e){const e=`[!${t}]`
o.setSelectionRange(s.index,s.index+s[0].length),o.focus(),document.execCommand("insertText",!1,e)
break}i++}}}static{dt7948.n(this.prototype,"onTypeChange",[e])}static{D(I({id:null,block:'[[[11,"blockquote"],[16,0,[28,[32,0],["callout",[52,[30,0,["isCollapsed"]],"is-collapsed"],[52,[30,0,["isCollapsible"]],"is-collapsible"]],null]],[16,"data-callout-type",[30,0,["type"]]],[16,"data-callout-alias",[30,0,["alias"]]],[4,[32,1],[[30,0,["setupColor"]]],null],[12],[1,"\\n"],[1,"  "],[11,0],[24,0,"callout-title"],[4,[32,2],["click",[52,[30,0,["isCollapsible"]],[30,0,["toggleCollapse"]],[28,[32,3],null,null]]],null],[4,[32,2],["mousedown",[30,0,["preventSelection"]]],null],[12],[1,"\\n"],[41,[30,1,["isPreview"]],[[[1,"      "],[8,[32,4],[[24,0,"btn-transparent"]],[["@value","@onChange"],[[28,[31,1],[[30,0,["type"]]],null],[30,0,["onTypeChange"]]]],null],[1,"\\n"]],[]],[[[41,[30,0,["icon"]],[[[1,"      "],[10,1],[14,0,"callout-icon"],[12],[1,"\\n        "],[1,[28,[32,5],[[30,0,["icon"]]],null]],[1,"\\n      "],[13],[1,"\\n    "]],[]],null]],[]]],[1,"    "],[10,1],[14,0,"callout-title-inner"],[12],[41,[30,1,["title","hasInline"]],[[[42,[28,[31,3],[[28,[31,3],[[30,1,["title","nodes"]]],null]],null],null,[[[1,[30,2]]],[2]],null]],[]],[[[1,[30,0,["title"]]]],[]]],[13],[1,"\\n"],[41,[30,0,["isCollapsible"]],[[[1,"      "],[10,1],[15,0,[28,[32,0],["callout-fold",[52,[30,0,["isCollapsed"]],"is-collapsed"]],null]],[12],[1,"\\n        "],[1,[28,[32,6],["chevron-down"],null]],[1,"\\n      "],[13],[1,"\\n"]],[]],null],[1,"  "],[13],[1,"\\n\\n"],[41,[30,1,["children"]],[[[1,"    "],[11,0],[24,0,"callout-content"],[4,[32,1],[[30,0,["setupContent"]]],null],[12],[1,"\\n"],[42,[28,[31,3],[[28,[31,3],[[30,1,["children"]]],null]],null],null,[[[41,[30,3,["isCallout"]],[[[1,"          "],[8,[32,7],null,[["@data"],[[30,3]]],null],[1,"\\n"]],[]],[[[1,"          "],[1,[30,3,["content"]]],[1,"\\n"]],[]]]],[3]],null],[1,"    "],[13],[1,"\\n"]],[]],null],[13]],["@data","node","child"],["if","readonly","each","-track-array"]]',moduleName:"(unknown template module)",scope:()=>[m,f,h,y,ve,Xt,g,Ce],isStrictMode:!0}),this)}}var we=Object.freeze({__proto__:null,default:Ce})
function Te(t,e,o={}){const{$from:n}=t.selection,l=Object.keys(o).length>0
for(let t=n.depth;t>=0;t--){const s=n.node(t)
if(s.type===e&&(!l||Object.keys(o).every(t=>s.attrs[t]===o[t])))return{node:s,depth:t,pos:n.before(t)}}return null}function Se(t){const{selection:e,schema:o}=t.state
if(e.node?.type===o.nodes.callout)return e.from
const n=Te(t.state,o.nodes.callout)
return n?.pos??null}function Ee(t,e,{title:o="",bodyNodes:n}={}){let l=ue(e)
l||(l=ue(e=re))
const s=o||l?.title||Jt(e),i=t.nodes.callout_title.create({type:e},t.text(s)),a=n||[t.nodes.paragraph.create()],r=t.nodes.callout_body.create(null,a)
return t.nodes.callout.create({type:e,hasCustomTitle:!!o},[i,r])}function ke(t,e,o,n){const{$from:l,$to:s}=o,i=l.before(l.depth),a=l.nodeBefore?.type===e.nodes.hard_break?o.from-1:o.from,r=s.nodeAfter?.type===e.nodes.hard_break?o.to+1:o.to
t.replaceWith(a,r,n)
let c=null
if(t.doc.nodesBetween(i,t.doc.content.size,(t,e)=>{if(!c&&t.type===n.type&&e>=i)return c=e,!1}),null==c)return null
const d=c+t.doc.nodeAt(c).nodeSize,u=t.doc.nodeAt(d)
u?.type!==e.nodes.paragraph||u.content.size||t.delete(d,d+u.nodeSize)
const p=t.doc.resolve(c).nodeBefore
return p?.type!==e.nodes.paragraph||p.content.size||(t.delete(c-p.nodeSize,c),c-=p.nodeSize),c}function Ne(t,e){let o=e
const n=[]
for(const e of t.children||[]){if(o<=0){n.push(e)
continue}if("text"!==e.type)continue
const t=e.content.length
t>o?(n.push({...e,content:e.content.slice(o)}),o=0):o-=t}t.children=n}function Ae(t){const e=t.children||[],o=[],n=[]
let l=!1
for(const t of e)if(l)n.push(t)
else if("softbreak"!==t.type&&"hardbreak"!==t.type){if("text"===t.type){const e=t.content.indexOf("\n")
if(-1!==e){o.push({...t,content:t.content.slice(0,e)})
const s=t.content.slice(e+1)
s&&n.push({...t,content:s}),l=!0
continue}}o.push(t)}else l=!0
return{title:o,rest:n}}var xe=Object.freeze({__proto__:null,activeCalloutPosFromView:Se,atBlockStart:function(t,e){let{$cursor:o}=t.selection
return!o||(e?!e.endOfTextblock("backward",t):o.parentOffset>0)?null:o},buildCallout:Ee,changedDescendants:function t(e,o,n,l=0){const s=e.childCount,i=o.childCount
t:for(let a=0,r=0;a<i;a++){const i=o.child(a)
for(let t=r,o=Math.min(s,a+5);t<o;t++)if(e.child(t)===i){r=t+1,l+=i.nodeSize
continue t}n(i,l),r<s&&e.child(r).sameMarkup(i)?t(e.child(r),i,n,l+1):i.nodesBetween(0,i.content.size,n,l+1),l+=i.nodeSize}},findAncestor:Te,inNode:function(t,e,o={}){return null!==Te(t,e,o)},insertBlockAtInlineSelection:ke,isNodeActive:function(t,e,o={}){const{from:n,to:l,empty:s}=t.selection,i=[]
t.doc.nodesBetween(n,l,(t,e)=>{if(t.isText)return
const o=Math.max(n,e),s=Math.min(l,e+t.nodeSize)
i.push({node:t,from:o,to:s})})
const a=l-n,r=i.filter(t=>e.name===t.node.type.name).filter(t=>!Object.keys(o).length||Object.keys(o).every(e=>t.node.attrs[e]===o[e]))
return s?!!r.length:r.reduce((t,e)=>t+e.to-e.from,0)>=a},splitAtFirstLineBreak:Ae,stripPrefix:Ne})
const Me=({schema:t,utils:e})=>({insertCallout:(o=re,n="")=>(l,s)=>{const{selection:i}=l,{$from:a,$to:r}=i,c=e=>Ee(t,o,{title:n,bodyNodes:e}),d=(e,{calloutStart:o,nearPos:n,minPos:l=0}={})=>{let s=o??null
if(null==s){const o=Math.min(e.doc.content.size,n+e.doc.content.size)
e.doc.nodesBetween(n,o,(e,o)=>{if(null==s&&e.type===t.nodes.callout&&o>=l)return s=o,!1})}if(null==s)return
const a=e.doc.nodeAt(s),r=s+a.nodeSize-2
e.setSelection(i.constructor.near(e.doc.resolve(r),-1))},u=l.tr
if(e.isNodeActive(l,t.nodes.callout_title)){for(let e=a.depth;e>=0;e--)if(a.node(e).type===t.nodes.callout){const t=a.before(e),o=a.node(e),n=c([o])
u.replaceWith(t,t+o.nodeSize,n),d(u,{calloutStart:t})
break}}else if(i.empty){const e=c([t.nodes.paragraph.create()])
u.replaceSelectionWith(e),d(u,{nearPos:a.pos,minPos:a.before(a.depth)})}else{if(a.parent===r.parent&&0===a.parentOffset&&r.parentOffset===a.parent.content.size&&a.parent.isBlock&&a.depth>0||a.parent!==r.parent){const t=a.blockRange(r)
if(!t)return!1
const e=c(l.doc.slice(t.start,t.end).content)
u.replaceWith(t.start,t.end,e),d(u,{calloutStart:t.start})}else{const e=l.doc.slice(i.from,i.to).content,o=c([t.nodes.paragraph.create(null,e)]),n=ke(u,t,i,o)
null!=n&&d(u,{calloutStart:n})}}return s?.(u.scrollIntoView()),!0}})
var Oe=Object.freeze({__proto__:null,commands:Me})
const Le=()=>{function t(t,e,o,n){const l=Ee(t.schema,e),s=o-1,i=t.tr.replaceWith(s,n,l),a=s+1+l.child(0).nodeSize+2
return i.setSelection(t.selection.constructor.near(i.doc.resolve(a))),i.scrollIntoView()}return[{match:/^\/callout(?::(\w+))?\s$/,handler:(e,o,n,l)=>t(e,o[1]?.toLowerCase()||re,n,l)},{match:/^!!(\w+)\s$/,handler:(e,o,n,l)=>t(e,o[1].toLowerCase(),n,l)}]}
var De=Object.freeze({__proto__:null,inputRules:Le})
const Ie={blockquote_open(t,e,o,n){t._bqStack||=[]
const l=o[n+2],s=(l?.content||"").split("\n")[0],i="paragraph_open"===o[n+1]?.type&&"inline"===l?.type&&s.match(ie)
if(!i)return t._bqStack.push("blockquote"),t.openNode(t.schema.nodes.blockquote),!0
const{callout:a,marker:r,fold:c}=i.groups
let d=a.toLowerCase(),u=ue(d)
u||(d=re,u=ue(d)),t._bqStack.push("callout"),Ne(l,r.length)
const p=l.children?.[0]
"text"===p?.type&&(p.content=p.content.replace(/^\s+/,""),p.content||l.children.shift())
const{title:h,rest:f}=Ae(l),m=h.length>0
if(m)l.children=h
else{const t=u?.title??Jt(d)
l.children=[{type:"text",content:t}]}const g={fold:c,isCollapsed:"-"===c,isCollapsible:["-","+"].includes(c),hasBody:f.length>0||"blockquote_close"!==o[n+4]?.type}
return f.length&&o.splice(n+4,0,{type:"paragraph_open",tag:"p",nesting:1},{type:"inline",children:f,content:""},{type:"paragraph_close",tag:"p",nesting:-1}),t.openNode(t.schema.nodes.callout,{type:d,hasCustomTitle:m,...g}),t.openNode(t.schema.nodes.callout_title,{type:d,...g}),!0},paragraph_open:t=>(t.top()?.type===t.schema.nodes.callout_title||t.openNode(t.schema.nodes.paragraph),!0),paragraph_close(t){const e=t.top()
return e?.type===t.schema.nodes.callout_title?(t.closeNode(),t.openNode(t.schema.nodes.callout_body),!0):e?.type===t.schema.nodes.paragraph&&(t.closeNode(),!0)},blockquote_close(t){const e=t._bqStack.pop(),o=()=>t.top()?.type
return"callout"===e?(o()===t.schema.nodes.callout_title&&t.closeNode(),o()===t.schema.nodes.callout_body&&t.closeNode(),o()===t.schema.nodes.callout&&t.closeNode(),!0):"blockquote"===e&&(o()===t.schema.nodes.blockquote&&t.closeNode(),!0)}},ze={callout(t,e){if(e.childCount<2){const o=1===e.childCount?e.child(0):null
return void("callout_body"===o?.type.name&&o.forEach((e,n,l)=>t.render(e,o,l)))}const o=e.child(0),n=e.child(1),l=t.out,s=t.delim,i=t.closed,a=t.atBlockStart
t.out="",t.delim="",t.closed=null,t.atBlockStart=!0,t.renderInline(o)
let r=t.out.trim()
t.out=l,t.delim=s,t.closed=i,t.atBlockStart=a
const c=e.attrs.fold||"",d=e.attrs.hasCustomTitle?` ${r}`:"",u=`[!${e.attrs.type}]${c}${d}`
t.wrapBlock("> ",null,e,()=>{t.write(n.childCount?`${u}\n`:u),n.childCount&&n.forEach((e,o,l)=>{if(l>0){const o="paragraph"===n.child(l-1).type.name&&"paragraph"!==e.type.name
t.flushClose(o?1:2)}t.render(e,n,l)})})}}
var Re=Object.freeze({__proto__:null,parse:Ie,serializeNode:ze})
class Pe extends u{static{dt7948.g(this.prototype,"appEvents",[s])}#l=void dt7948.i(this,"appEvents")
static{dt7948.g(this.prototype,"calloutSettings",[s])}#t=void dt7948.i(this,"calloutSettings")
static{dt7948.g(this.prototype,"calloutMoveState",[s])}#s=void dt7948.i(this,"calloutMoveState")
static{dt7948.g(this.prototype,"activeCalloutPos",[p],function(){return null})}#i=void dt7948.i(this,"activeCalloutPos")
static{dt7948.g(this.prototype,"hasEmptyBody",[p],function(){return!1})}#a=void dt7948.i(this,"hasEmptyBody")
static{dt7948.g(this.prototype,"needsInsertAfter",[p],function(){return!1})}#r=void dt7948.i(this,"needsInsertAfter")
static{dt7948.g(this.prototype,"canNestUp",[p],function(){return!1})}#c=void dt7948.i(this,"canNestUp")
static{dt7948.g(this.prototype,"canNestDown",[p],function(){return!1})}#d=void dt7948.i(this,"canNestDown")
static{dt7948.g(this.prototype,"canMoveUp",[p],function(){return!1})}#u=void dt7948.i(this,"canMoveUp")
static{dt7948.g(this.prototype,"canMoveDown",[p],function(){return!1})}#p=void dt7948.i(this,"canMoveDown")
constructor(){super(...arguments),this.args.onSetup?.(this),this.activeCalloutPos=Se(this.args.view),this.updateState(),this.appEvents.on("callout:selection-changed",this,this.onSelectionChanged)}willDestroy(){super.willDestroy(),this.appEvents.off("callout:selection-changed",this,this.onSelectionChanged)}onSelectionChanged(t){this.activeCalloutPos=t,this.updateState()}get isSelected(){return this.activeCalloutPos===this.args.getPos()}get showMoveControls(){return this.calloutMoveState.isEnabledFor(this.args.getPos())}updateState(){const t=this.args.getPos()
if(null==t)return this.canMoveUp=!1,this.canMoveDown=!1,this.canNestUp=!1,this.canNestDown=!1,this.hasEmptyBody=!1,void(this.needsInsertAfter=!1)
const e=this.args.dom?.querySelector(".callout-content")
this.hasEmptyBody=!!e&&0===e.childElementCount
const{state:o}=this.args.view,{schema:n}=o,l=o.doc.resolve(t),s=l.index(l.depth),i=s>0?l.parent.child(s-1):null,a=s<l.parent.childCount-1?l.parent.child(s+1):null
this.canNestUp=i?.type===n.nodes.callout,this.canNestDown=a?.type===n.nodes.callout
const r=l.parent.type===n.nodes.callout_body,c=t=>t?.type===n.nodes.paragraph&&0===t.content.size,d=1===s&&c(i),u=s===l.parent.childCount-2&&c(a),p=s>0&&!d,h=s<l.parent.childCount-1&&!u
this.canMoveUp=p||r,this.canMoveDown=h||r
const f=o.doc.nodeAt(t)
if(!f)return void(this.needsInsertAfter=!1)
const m=t+f.nodeSize,g=o.doc.resolve(m).nodeAfter
this.needsInsertAfter=!g||g.type===n.nodes.callout}#h(){const{view:t}=this.args,{state:e}=t,{schema:o}=e,n=this.args.getPos()
if(null==n)return null
const l=e.doc.nodeAt(n)
if(!l)return null
const s=e.doc.resolve(n),i=s.index(s.depth)
return{view:t,state:e,schema:o,calloutPos:n,calloutNode:l,$pos:s,index:i}}#f(t,e){const{view:o}=this.args,n=t.doc.resolve(e+2)
t.setSelection(o.state.selection.constructor.near(n)),this.calloutMoveState.enable(e),o.dispatch(t.scrollIntoView()),o.focus(),this.appEvents.trigger("callout:selection-changed",e)}addBody(){const t=this.#h()
if(!t)return
const{view:e,state:o,schema:n,calloutPos:l,calloutNode:s}=t,i=l+1+s.child(0).nodeSize+1,a=o.tr.insert(i,n.nodes.paragraph.create()),r=a.doc.resolve(i+1)
a.setSelection(o.selection.constructor.near(r)),e.dispatch(a.scrollIntoView()),e.focus()}static{dt7948.n(this.prototype,"addBody",[e])}insertAfter(){const t=this.#h()
if(!t)return
const{view:e,state:o,schema:n,calloutPos:l,calloutNode:s}=t,i=l+s.nodeSize,a=o.tr.insert(i,n.nodes.paragraph.create()),r=a.doc.resolve(i+1)
a.setSelection(o.selection.constructor.near(r)),e.dispatch(a.scrollIntoView()),e.focus()}static{dt7948.n(this.prototype,"insertAfter",[e])}moveUp(){const t=this.#h()
if(!t)return
const{state:e,schema:o,calloutPos:n,calloutNode:l,$pos:s,index:i}=t,a=e.tr
let r
if(i>0){const t=n-s.parent.child(i-1).nodeSize
a.insert(t,l)
const e=n+l.nodeSize
a.delete(e,e+l.nodeSize),r=t}else{if(s.parent.type!==o.nodes.callout_body)return
{const t=s.before(s.depth-1)
a.insert(t,l)
const e=n+l.nodeSize
a.delete(e,e+l.nodeSize),r=t}}this.#f(a,r)}static{dt7948.n(this.prototype,"moveUp",[e])}moveDown(){const t=this.#h()
if(!t)return
const{state:e,schema:o,calloutPos:n,calloutNode:l,$pos:s,index:i}=t,a=e.tr
let r
if(i<s.parent.childCount-1){const t=n+l.nodeSize,o=e.doc.nodeAt(t)
if(!o)return
a.insert(t+o.nodeSize,l),a.delete(n,n+l.nodeSize),r=n+o.nodeSize}else{if(s.parent.type!==o.nodes.callout_body)return
{const t=s.after(s.depth-1)
a.insert(t,l),a.delete(n,n+l.nodeSize),r=t-l.nodeSize}}this.#f(a,r)}static{dt7948.n(this.prototype,"moveDown",[e])}nestUp(){const t=this.#h()
if(!t)return
const{state:e,schema:o,calloutPos:n,calloutNode:l,$pos:s,index:i}=t
if(0===i)return
if(s.parent.child(i-1).type!==o.nodes.callout)return
const a=n-2,r=e.tr
r.insert(a,l)
const c=n+l.nodeSize
r.delete(c,c+l.nodeSize),this.#f(r,a)}static{dt7948.n(this.prototype,"nestUp",[e])}nestDown(){const t=this.#h()
if(!t)return
const{state:e,schema:o,calloutPos:n,calloutNode:l,$pos:s,index:i}=t
if(i>=s.parent.childCount-1)return
if(s.parent.child(i+1).type!==o.nodes.callout)return
const a=n+l.nodeSize,r=e.doc.nodeAt(a)
if(!r)return
const c=a+1+r.child(0).nodeSize+1,d=e.tr
d.insert(c,l),d.delete(n,n+l.nodeSize),this.#f(d,c-l.nodeSize)}static{dt7948.n(this.prototype,"nestDown",[e])}update(t){const e=this.calloutSettings.find(t.attrs.type),o=this.args.dom.firstElementChild
e?e.mainType?(o.setAttribute("data-callout-type",e.mainType),o.setAttribute("data-callout-alias",e.type)):(o.removeAttribute("data-callout-alias"),o.setAttribute("data-callout-type",e.type)):(o.removeAttribute("data-callout-alias"),o.setAttribute("data-callout-type",re)),o.classList.toggle("is-collapsed",t.attrs.isCollapsed),o.classList.toggle("is-collapsible",t.attrs.isCollapsible),this.updateState()}static{D(I({id:null,block:'[[[41,[30,0,["isSelected"]],[[[41,[30,0,["showMoveControls"]],[[[41,[28,[32,0],[[30,0,["canMoveUp"]],[30,0,["canNestUp"]]],null],[[[1,"      "],[10,0],[14,0,"callout-move-controls callout-top-controls"],[14,"contenteditable","false"],[12],[1,"\\n"],[41,[30,0,["canMoveUp"]],[[[1,"          "],[8,[32,1],[[24,0,"callout-move-btn callout-move-up-btn btn btn-flat btn-small"]],[["@icon","@action","@translatedTitle","@preventFocus"],["arrow-up",[30,0,["moveUp"]],[28,[32,2],[[28,[32,3],["composer.menu.move_up"],null]],null],"true"]],null],[1,"\\n"]],[]],null],[41,[30,0,["canNestUp"]],[[[1,"          "],[8,[32,1],[[24,0,"callout-move-btn callout-nest-btn btn btn-flat btn-small"]],[["@icon","@action","@translatedTitle","@preventFocus"],["arrow-up-from-bracket",[30,0,["nestUp"]],[28,[32,2],[[28,[32,3],["composer.menu.nest_up"],null]],null],"true"]],null],[1,"\\n"]],[]],null],[1,"      "],[13],[1,"\\n"]],[]],null]],[]],[[[1,"    "],[10,0],[14,0,"callout-handle"],[14,"contenteditable","false"],[12],[1,"\\n      "],[1,[28,[32,4],["grip-lines"],null]],[1,"\\n    "],[13],[1,"\\n"]],[]]],[1,"  "],[10,0],[14,0,"callout-bottom-controls"],[14,"contenteditable","false"],[12],[1,"\\n"],[41,[30,0,["hasEmptyBody"]],[[[1,"      "],[8,[32,1],[[24,0,"callout-add-body btn btn-flat btn-small"]],[["@icon","@action","@translatedTitle","@preventFocus"],["callout-add-body",[30,0,["addBody"]],[28,[32,2],[[28,[32,3],["composer.menu.add_body"],null]],null],"true"]],null],[1,"\\n"]],[]],null],[1,"\\n"],[41,[30,0,["showMoveControls"]],[[[1,"\\n"],[41,[30,0,["canMoveDown"]],[[[1,"        "],[8,[32,1],[[24,0,"callout-move-btn callout-move-down-btn btn btn-flat btn-small"]],[["@icon","@action","@translatedTitle","@preventFocus"],["arrow-down",[30,0,["moveDown"]],[28,[32,2],[[28,[32,3],["composer.menu.move_down"],null]],null],"true"]],null],[1,"\\n"]],[]],null],[41,[30,0,["canNestDown"]],[[[1,"        "],[8,[32,1],[[24,0,"callout-move-btn callout-nest-btn callout-nest-down btn btn-flat btn-small"]],[["@icon","@action","@translatedTitle","@preventFocus"],["arrow-up-from-bracket",[30,0,["nestDown"]],[28,[32,2],[[28,[32,3],["composer.menu.nest_down"],null]],null],"true"]],null],[1,"\\n"]],[]],null]],[]],null],[1,"\\n"],[41,[30,0,["needsInsertAfter"]],[[[1,"      "],[8,[32,1],[[24,0,"callout-add-after btn btn-flat btn-small"]],[["@icon","@action","@translatedTitle","@preventFocus"],["callout-insert-after",[30,0,["insertAfter"]],[28,[32,2],[[28,[32,3],["composer.menu.insert_paragraph_after"],null]],null],"true"]],null],[1,"\\n"]],[]],null],[1,"  "],[13],[1,"\\n"]],[]],null]],[],["if"]]',moduleName:"(unknown template module)",scope:()=>[M,E,d,B,g],isStrictMode:!0}),this)}}var Be=Object.freeze({__proto__:null,default:Pe})
class Ue extends u{static{dt7948.g(this.prototype,"appEvents",[s])}#l=void dt7948.i(this,"appEvents")
static{dt7948.g(this.prototype,"calloutSettings",[s])}#t=void dt7948.i(this,"calloutSettings")
static{dt7948.g(this.prototype,"calloutMoveState",[s])}#s=void dt7948.i(this,"calloutMoveState")
static{dt7948.g(this.prototype,"capabilities",[s])}#m=void dt7948.i(this,"capabilities")
static{dt7948.g(this.prototype,"type",[p])}#g=void dt7948.i(this,"type")
static{dt7948.g(this.prototype,"fold",[p])}#y=void dt7948.i(this,"fold")
static{dt7948.g(this.prototype,"isCollapsed",[p])}#n=void dt7948.i(this,"isCollapsed")
static{dt7948.g(this.prototype,"isCollapsible",[p])}#b=void dt7948.i(this,"isCollapsible")
static{dt7948.g(this.prototype,"hasBody",[p])}#v=void dt7948.i(this,"hasBody")
static{dt7948.g(this.prototype,"activeCalloutPos",[p],function(){return null})}#i=void dt7948.i(this,"activeCalloutPos")
constructor(){super(...arguments)
const{type:t,fold:e,isCollapsed:o,isCollapsible:n,hasBody:l}=this.args.node.attrs
this.type=t,this.fold=e||"",this.isCollapsed=o,this.isCollapsible=n,this.hasBody=l,this.args.onSetup?.(this),this.activeCalloutPos=Se(this.args.view),this.appEvents.on("callout:selection-changed",this,this.onSelectionChanged)}willDestroy(){super.willDestroy(),this.appEvents.off("callout:selection-changed",this,this.onSelectionChanged)}onSelectionChanged(t){this.activeCalloutPos=t}get isSelected(){return this.activeCalloutPos===this.args.getPos()-1}get isMoveEnabled(){return this.calloutMoveState.isEnabledFor(this.args.getPos()-1)}get isNested(){const{state:t}=this.args.view,e=this.args.getPos()-1
return t.doc.resolve(e).parent.type===t.schema.nodes.callout_body}get canShowMoreOptions(){return this.capabilities.touch||this.hasBody||this.isCollapsible}ignoreMutation(t){const{target:e,type:o}=t,n=e.nodeType===Node.ELEMENT_NODE?e:e.parentElement
if(!n)return!0
const l=n.closest(".callout-left-controls")||n.closest(".callout-right-controls"),s=n.closest(".callout-title-inner")
return!(!l&&s)||"characterData"!==o&&"selection"!==o}get calloutType(){return this.args.node.attrs.type||this.calloutSettings.fallbackType}update(t){this.type=t.attrs.type,this.fold=t.attrs.fold||"",this.isCollapsed=t.attrs.isCollapsed,this.isCollapsible=t.attrs.isCollapsible,this.hasBody=t.attrs.hasBody}updateNodeMarkup(t){const{state:e,dispatch:o}=this.args.view,n=this.args.getPos(),l=n-1
let s=e.tr
const i=e.doc.nodeAt(l)
i&&"callout"===i.type.name&&(s=s.setNodeMarkup(l,null,{...i.attrs,...t}))
const a=s.doc.nodeAt(n)
a&&"callout_title"===a.type.name&&(s=s.setNodeMarkup(n,null,{...a.attrs,...t})),o(s)}focusEditor(){S(()=>{const{view:t}=this.args
t.dom?.isConnected&&t.focus()})}static{dt7948.n(this.prototype,"focusEditor",[e])}onTypeChange(t){this.type=t,this.updateNodeMarkup({type:this.type})
const{state:e,dispatch:o}=this.args.view,n=this.args.getPos(),l=e.doc.nodeAt(n-1)
if(l&&!l.attrs.hasCustomTitle){const l=ue(t),s=l?.title||Jt(t),i=e.doc.nodeAt(n)
if(i){const t=n+1,l=t+i.content.size,a=e.tr.replaceWith(t,l,s?e.schema.text(s):[])
a.setMeta("callout:isDefaultTitle",!0),o(a)}}S(()=>{this.focusEditor()})}static{dt7948.n(this.prototype,"onTypeChange",[e])}setFold(t){this.fold=t,this.isCollapsed="-"===t,this.isCollapsible=""!==t,this.updateNodeMarkup({fold:this.fold,isCollapsed:this.isCollapsed,isCollapsible:this.isCollapsible})}static{dt7948.n(this.prototype,"setFold",[e])}deleteCallout(){const{view:t}=this.args,{schema:e}=t.state,o=Te(t.state,e.nodes.callout)
if(!o)return
const n=t.state.tr
n.delete(o.pos,o.pos+o.node.nodeSize),t.dispatch(n),t.focus()}static{dt7948.n(this.prototype,"deleteCallout",[e])}toggleMoveControls(){const t=this.args.getPos()-1
this.calloutMoveState.toggle(t)}static{dt7948.n(this.prototype,"toggleMoveControls",[e])}get foldOptions(){const t=t=>d(B(`composer.menu.folding_options.${t}`))
return[{value:"",label:t("none"),className:"option-none"},{value:"-",label:t("collapsed"),className:"option-collapsed"},{value:"+",label:t("expanded"),className:"option-expanded"}]}toggleCollapse(){const t=!this.isCollapsed
Zt(this.args.dom.parentElement.querySelector(".callout-content"),t,t=>{this.isCollapsed=t,this.updateNodeMarkup({isCollapsed:t})})}static{dt7948.n(this.prototype,"toggleCollapse",[e])}static{D(I({id:null,block:'[[[10,1],[15,0,[28,[32,0],["callout-controls-hub",[52,[30,0,["isSelected"]],"is-selected"]],null]],[12],[1,"\\n  "],[10,1],[14,0,"callout-left-controls"],[14,"contenteditable","false"],[12],[1,"\\n    "],[8,[32,1],null,[["@value","@onChange","@onClose","@disabled"],[[30,0,["type"]],[30,0,["onTypeChange"]],[30,0,["focusEditor"]],[28,[32,2],[[30,0,["isSelected"]]],null]]],null],[1,"\\n  "],[13],[1,"\\n\\n"],[41,[30,0,["isSelected"]],[[[1,"    "],[10,1],[14,0,"callout-right-controls"],[14,"contenteditable","false"],[12],[1,"\\n"],[41,[30,0,["canShowMoreOptions"]],[[[1,"        "],[8,[32,3],null,[["@identifier","@icon","@class","@translatedTitle"],["callout-options-menu","ellipsis-vertical","callout-control-btn btn-no-text btn-transparent",[28,[32,4],[[28,[32,5],["composer.menu.more_options"],null]],null]]],[["content"],[[[[1,"\\n            "],[8,[32,6],null,[["@class"],["callout-control-dropdown"]],[["default"],[[[[1,"\\n"],[41,[30,0,["capabilities","touch"]],[[[1,"                "],[8,[30,1,["item"]],[[24,0,"callout-control-dropdown__move-item"]],null,[["default"],[[[[1,"\\n                  "],[10,1],[12],[1,[28,[32,4],[[28,[32,5],["composer.menu.move"],null]],null]],[13],[1,"\\n                  "],[8,[32,7],[[4,[32,8],["click",[30,0,["toggleMoveControls"]]],null]],[["@state"],[[30,0,["isMoveEnabled"]]]],null],[1,"\\n                "]],[]]]]],[1,"\\n"]],[]],null],[1,"\\n"],[41,[28,[32,9],[[30,0,["hasBody"]],[30,0,["isCollapsible"]]],null],[[[1,"                "],[8,[30,1,["item"]],[[24,0,"callout-control-dropdown__fold-item"]],null,[["default"],[[[[1,"\\n                  "],[10,1],[12],[1,[28,[32,4],[[28,[32,5],["composer.menu.folding"],null]],null]],[13],[1,"\\n"],[42,[28,[31,2],[[28,[31,2],[[30,0,["foldOptions"]]],null]],null],null,[[[1,"                    "],[8,[32,10],[[16,0,[28,[32,0],["callout-control-fold","text-size btn btn-flat",[30,2,["className"]],[52,[28,[32,11],[[30,2,["value"]],[30,0,["fold"]]],null],"active"]],null]]],[["@action"],[[28,[32,12],[[30,0,["setFold"]],[30,2,["value"]]],null]]],[["default"],[[[[1,"\\n                      "],[1,[30,2,["label"]]],[1,"\\n                    "]],[]]]]],[1,"\\n"]],[2]],null],[1,"                "]],[]]]]],[1,"\\n"]],[]],null],[1,"            "]],[1]]]]],[1,"\\n          "]],[]]]]],[1,"\\n"]],[]],null],[1,"\\n      "],[8,[32,10],[[24,0,"callout-control-btn callout-delete-btn btn-no-text btn-transparent"]],[["@icon","@action","@translatedTitle"],["trash-can",[30,0,["deleteCallout"]],[28,[32,4],[[28,[32,5],["composer.delete_callout"],null]],null]]],null],[1,"\\n    "],[13],[1,"\\n"]],[]],null],[13],[1,"\\n\\n"],[41,[28,[32,13],[[30,0,["isCollapsible"]],[30,0,["hasBody"]]],null],[[[1,"  "],[8,[32,10],[[16,0,[28,[32,0],["callout-fold btn btn-no-text btn-transparent",[52,[30,0,["isCollapsed"]],"is-collapsed"]],null]]],[["@icon","@action","@preventFocus","@translatedTitle"],["chevron-down",[30,0,["toggleCollapse"]],true,[28,[32,4],[[28,[32,5],["composer.menu.toggle_folding"],null]],null]]],null],[1,"\\n"]],[]],null]],["dropdown","option"],["if","each","-track-array"]]',moduleName:"(unknown template module)",scope:()=>[m,ve,O,w,d,B,k,z,h,M,E,x,T,L],isStrictMode:!0}),this)}}var Fe=Object.freeze({__proto__:null,default:Ue})
class He{static{dt7948.g(this.prototype,"node",[p])}#_=void dt7948.i(this,"node")
#C
constructor({node:t,view:e,getPos:o,getContext:n,component:l,name:s,hasContent:i=!1,buildDOM:a=null}){if(this.node=t,this.view=e,this.getPos=o,this.getContext=n,this.component=l,n().addGlimmerNodeView(this),a){const{dom:e,contentDOM:o}=a(t)
this.dom=e,this.contentDOM=o}else this.dom=document.createElement(t.isInline?"span":"div"),this.dom.classList.add(`composer-${s}-node`),i&&(this.contentDOM=document.createElement(t.isInline?"span":"div"),this.dom.appendChild(this.contentDOM))}setComponentInstance(t){this.#C=t,this.#C?.setSelection?this.setSelection=this.#C.setSelection.bind(this.#C):this.setSelection=void 0}static{dt7948.n(this.prototype,"setComponentInstance",[e])}update(t,e,o){return this.node=t,this.#C?.update?.(t,e,o)??!0}selectNode(){S(()=>this.#C?.selectNode?.())}deselectNode(){S(()=>this.#C?.deselectNode?.())}stopEvent(t){return this.#C?.stopEvent?.(t)??!1}ignoreMutation(t){return this.#C?.ignoreMutation?.(t)??!0}destroy(){this.#C?.destroy?.(),this.#C=null,this.getContext().removeGlimmerNodeView(this)}}var qe=Object.freeze({__proto__:null,default:He})
const $e={callout:({getContext:t})=>(e,o,n)=>{const l="callout"
return new He({node:e,view:o,getPos:n,getContext:t,component:Pe,name:l,buildDOM(){const t=document.createElement("div")
t.className=`composer-${l}-node`
const o=document.createElement("blockquote")
o.classList.add(l),o.setAttribute("data-callout-type",e.attrs.type)
const n=ue(e.attrs.type)
return n?n?.mainType?(o.setAttribute("data-callout-type",n.mainType),o.setAttribute("data-callout-alias",n.type)):o.setAttribute("data-callout-type",n.type):o.setAttribute("data-callout-type",re),e.attrs.isCollapsed&&o.classList.add("is-collapsed"),e.attrs.isCollapsible&&o.classList.add("is-collapsible"),t.appendChild(o),{dom:t,contentDOM:o}}})},callout_title:({getContext:t})=>(e,o,n)=>{const l="callout-title"
return new He({node:e,view:o,getPos:n,getContext:t,component:Ue,name:l,buildDOM(){const t=document.createElement("div")
t.className=`composer-${l}-node ${l}`
const e=document.createElement("span")
return e.className=`${l}-inner`,t.appendChild(e),{dom:t,contentDOM:e}}})},callout_body:class{constructor(){const t=document.createElement("div")
t.className="callout-content",this.dom=t,this.contentDOM=t}ignoreMutation(t){return"selection"!==t.type}}}
var je=Object.freeze({__proto__:null,nodeViews:$e})
function We({view:t,$from:e,schema:o,dispatch:n,state:l,TextSelection:s}){if(!t.endOfTextblock("down"))return!1
const i=e.parent,a=i.type===o.nodes.paragraph&&0===i.content.size,r=Te(l,o.nodes.callout_title)
if(r){const t=e.node(r.depth-1),i=t.childCount>1?t.child(1):null
if(i&&0===i.content.size){const t=r.pos+r.node.nodeSize+1,e=l.tr.insert(t,o.nodes.paragraph.create())
return e.setMeta("callout:keyboardNav",!0),e.setSelection(s.create(e.doc,t+1)),n(e.scrollIntoView()),!0}return!1}if(a){const t=Te(l,o.nodes.callout_body)
if(t){if(e.index(e.depth-1)+1===t.node.childCount){const t=e.before(e.depth),a=Te(l,o.nodes.callout),r=a.pos+a.node.nodeSize
let c=l.tr
c.delete(t,t+i.nodeSize)
const d=r-i.nodeSize
return l.doc.nodeAt(r)||c.insert(d,o.nodes.paragraph.create()),c.setMeta("callout:keyboardNav",!0),c.setSelection(s.create(c.doc,d+1)),n(c.scrollIntoView()),!0}}}const c=Te(l,o.nodes.callout)
if(c){if(e.index(c.depth-1)+1===e.node(c.depth-1).childCount){const t=e.after(e.depth),i=c.pos+c.node.nodeSize
if(i-t>2)return!1
const a=i,r=l.tr.insert(a,o.nodes.paragraph.create())
return r.setMeta("callout:keyboardNav",!0),r.setSelection(s.create(r.doc,a+1)),n(r.scrollIntoView()),!0}}return!1}function Ge({view:t,$from:e,schema:o,dispatch:n,state:l,TextSelection:s}){if(t.endOfTextblock("up")){const t=e.index(e.depth-1)
if(t>0){const i=e.node(e.depth-1).child(t-1)
if(i.type===o.nodes.callout){const t=i.child(1).lastChild
if(!t||t.type===o.nodes.callout){const t=e.before(e.depth)-2,i=l.tr
return e.parent.type===o.nodes.paragraph&&0===e.parent.content.size&&i.delete(e.before(e.depth),e.after(e.depth)),i.insert(t,o.nodes.paragraph.create()),i.setSelection(s.create(i.doc,t+1)),n(i.scrollIntoView()),!0}if(t){const t=l.tr
e.parent.type===o.nodes.paragraph&&0===e.parent.content.size&&t.delete(e.before(e.depth),e.after(e.depth))
const i=e.before(e.depth)-1
return t.setSelection(s.near(t.doc.resolve(i),-1)),n(t.scrollIntoView()),!0}}}}if(e.parent.type===o.nodes.paragraph&&0===e.parent.content.size){const t=e.node(e.depth-1),i=e.index(e.depth-1)
if(i>0){const a=t.child(i-1)
if(a.type===o.nodes.callout){const t=l.tr.delete(e.before(e.depth),e.after(e.depth)),i=a.child(1).lastChild,r=i?.type===o.nodes.paragraph&&0===i.content.size,c=e.before(e.depth)-2
return r?(t.setMeta("callout:keyboardNav",!0),t.setSelection(s.create(t.doc,c))):(t.insert(c,o.nodes.paragraph.create()),t.setMeta("callout:keyboardNav",!0),t.setSelection(s.create(t.doc,c+1))),n(t.scrollIntoView()),!0}}const a=Te(l,o.nodes.callout_body)
if(a&&1===a.node.childCount){const t=Te(l,o.nodes.callout)
if(t){const o=t.node.child(0),i=t.pos+2+o.content.size,a=e.before(e.depth),r=l.tr.delete(a,a+e.parent.nodeSize)
return r.setMeta("callout:keyboardNav",!0),r.setSelection(s.create(r.doc,i)),n(r.scrollIntoView()),!0}}}return!1}function Ve({$from:t,schema:e,dispatch:o,state:n,TextSelection:l},s){const i=Te(n,e.nodes.callout_title)
if(!i)return!1
const{node:a,depth:r,pos:c}=i,d=t.node(r-1)
if(d.type!==e.nodes.callout||d.childCount<2)return!1
const u=d.child(1)
if(u.type!==e.nodes.callout_body)return!1
s.preventDefault()
const p=t.after(r)+1
let h=n.tr
if(d.attrs.isCollapsed){const e=t.before(r-1)
h=h.setNodeMarkup(e,null,{...d.attrs,isCollapsed:!1,fold:"+"}).setNodeMarkup(c,null,{...a.attrs,isCollapsed:!1,fold:"+"})}return 0!==u.childCount&&u.firstChild.type===e.nodes.paragraph||(h=h.insert(p,e.nodes.paragraph.create())),o(h.setMeta("callout:keyboardNav",!0).setSelection(l.create(h.doc,p+1)).scrollIntoView()),!0}function Ye({view:t,$from:e,schema:o,state:n}){const l=Te(n,o.nodes.callout_title)
if(!l)return!1
if(0!==e.parentOffset)return!1
const s=t.nodeDOM(l.pos),i=s?.closest(".composer-callout-node")?.querySelector(".callout-chooser-trigger")
return!!i&&(i.click(),!0)}var Xe=Object.freeze({__proto__:null,handleArrowDown:We,handleArrowLeft:Ye,handleArrowUp:Ge,handleEnter:Ve})
const Ke=/^\s*>\s?/
function Ze(t,e,o){const n=[]
let l=e
return t.forEach(t=>{if(t.isText&&l>0){const e=t.text.length
e>l&&n.push(o.text(t.text.slice(l),t.marks)),l-=e}else n.push(t)}),t.constructor.from(n)}function Qe(t,e){const o=t.firstChild,n=o?.type===e.nodes.paragraph&&o.textContent.match(ie)
if(!n)return null
const{callout:l,fold:s,marker:i}=n.groups,a=l.toLowerCase(),r=s||"",c=Ze(o.content,i.length,e),d=ue(a)?.title??Jt(a),u=e.nodes.callout_title.create({type:a,fold:r},c.size?c:[e.text(d)]),p=Array.from({length:t.childCount-1},(e,o)=>t.child(o+1))
return p.length||p.push(e.nodes.paragraph.create()),e.nodes.callout.create({type:a,fold:r},[u,e.nodes.callout_body.create(null,p)])}function Je(t,e){const o=[]
let n=[],l=!1
const s=()=>{if(!n.length)return
l=!0
const s=t.constructor.from(n)
n=[]
const i=Je(s,e),a=e.nodes.blockquote.create(null,i)
o.push(Qe(a,e)||a)}
return t.forEach(t=>{const i=t.type===e.nodes.paragraph&&t.textContent.match(Ke)
if(i)return void n.push(t.copy(Ze(t.content,i[0].length,e)))
s()
let a=t
const r=Je(t.content,e)
if(r!==t.content&&(a=t.copy(r),l=!0),a.type===e.nodes.blockquote){const t=Qe(a,e)
t&&(a=t,l=!0)}o.push(a)}),s(),l?t.constructor.from(o):t}var to=Object.freeze({__proto__:null,transformFragmentsToCallouts:Je})
function eo({pmState:{Plugin:t,TextSelection:e,PluginKey:o},pmView:{Decoration:n,DecorationSet:l},pmModel:{Fragment:s,Slice:i},utils:{convertFromMarkdown:a},getContext:r}){const c=new t({key:new o("callout"),props:{decorations(t){const{doc:e,schema:o}=t,s=o.nodes.callout
if(!s)return null
const i=[]
return e.descendants((t,e)=>{if(t.type!==s)return
const o=ue(t.attrs.type||re),l=o?.color||P.callout_fallback_color,a=Qt(l,P.callout_background_opacity/100),r=Qt(l,.3)
l&&i.push(n.node(e,e+t.nodeSize,{style:`\n                  --q-callout-background: ${a}; \n                  --q-callout-color-darker: ${r}; \n                  --q-callout-color: ${l};`}))}),l.create(e,i)},handlePaste(t,e){const o=e.clipboardData,n=o?.types?.includes("text/html"),l=o?.getData("text/plain")
if(n&&l&&ae.test(l)){const e=a(l),o=i.maxOpen(s.from(e.content)),n=t.state.tr.replaceSelection(o)
return t.dispatch(n),!0}return!1},transformPasted(t,e){const o=e.state.schema,n=Je(t.content,o)
return n!==t.content?new t.constructor(n,t.openStart,t.openEnd):t},handleClick(t,o){const{state:n,dispatch:l}=t,s=n.doc.resolve(o),{callout:i,callout_body:a,paragraph:r}=n.schema.nodes
if(!i||!a||!r)return!1
if(t.dom.querySelector(".callout-chooser-trigger.-expanded"))return!1
const c=s.parent
if(c.type===a||c.type===i){if(c.type===i&&(c.lastChild?.type!==a||s.parentOffset<=c.child(0).nodeSize))return!1
if(c.type===a&&s.parentOffset<c.content.size)return!1
const t=(c.type===a?c:c.lastChild).lastChild
if(!t||t.type!==i)return!1
const o=c.type===a?s.end(s.depth):s.end(s.depth)-1,d=n.tr.insert(o,r.create())
return d.setSelection(e.create(d.doc,o+1)),l(d.scrollIntoView()),!0}return!1},handleKeyDown(t,o){const{state:n,dispatch:l}=t,{selection:s,schema:i}=n,{$from:a,empty:r}=s
if(!r)return!1
const c={view:t,$from:a,schema:i,dispatch:l,state:n,TextSelection:e},d={ArrowDown:()=>We(c),ArrowUp:()=>Ge(c),Enter:()=>Ve(c,o)}
return d[o.key]?.()||!1},handleDOMEvents:{keydown(t,e){if("ArrowLeft"===e.key){const{state:o}=t,{selection:n,schema:l}=o,{$from:s,empty:i}=n
if(i&&Ye({view:t,$from:s,schema:l,state:o}))return e.preventDefault(),!0}return!1},dragstart(t,e){e.target.nodeType===Node.ELEMENT_NODE&&e.target.closest(".composer-callout-node")?.classList.add("is-dragging")},dragend(t,e){e.target.nodeType===Node.ELEMENT_NODE&&e.target.closest(".composer-callout-node")?.classList.remove("is-dragging")}}},appendTransaction(t,e,o){const{schema:n}=o
if(!n.nodes.callout_title)return null
const l=t.some(t=>t.getMeta("callout:isDefaultTitle"))
if(t.some(t=>t.docChanged)){let s=null
if(o.doc.descendants((i,a)=>{if(i.type!==n.nodes.callout)return
if(!l&&!i.attrs.hasCustomTitle){const l=i.child(0)
if(l.content.size>0){let r=a
try{for(let e=t.length-1;e>=0;e--)r=t[e].mapping.invert().map(r)}catch{return!1}const c=e.doc.nodeAt(r)
if(!c||c.type!==n.nodes.callout)return!1
c.child(0).content.eq(l.content)||(s||(s=o.tr),s.setNodeMarkup(a,null,{...i.attrs,hasCustomTitle:!0}))}}const r=i.child(1).childCount>0
if(i.attrs.hasBody!==r){s||(s=o.tr)
const t=s.doc.nodeAt(a)
s.setNodeMarkup(a,null,{...t.attrs,hasBody:r})
const e=a+1,l=s.doc.nodeAt(e)
l?.type===n.nodes.callout_title&&s.setNodeMarkup(e,null,{...l.attrs,hasBody:r})}}),s)return s}if(t.some(t=>t.selectionSet)){const l=n.nodes.callout_title,s=Te(e,l)
if(s){let e=s.pos
for(const o of t)try{e=o.mapping.map(e)}catch{return null}const n=o.doc.nodeAt(e)
if(n&&n.type===l&&0===n.content.size&&Te(o,l)?.pos!==e){const{type:t}=n.attrs,l=ue(t),s=l?.title||Jt(t),i=e-1,a=o.tr.insertText(s,e+1).setMeta("callout:isDefaultTitle",!0),r=a.doc.nodeAt(i)
return r?.attrs.hasCustomTitle&&a.setNodeMarkup(i,null,{...r.attrs,hasCustomTitle:!1}),a}}}return null}}),d=new t({key:new o("calloutSelection"),props:{handleClickOn(t,o,n,l,s){if("callout"!==n.type.name)return!1
let i=s.target
const a=t.state.doc.resolve(o)
if(!i.classList.contains("callout-title-inner"))if("callout_title"===a.nodeBefore?.type.name&&"callout_body"===a.nodeAfter?.type.name)i=i.querySelector(".callout-title-inner")
else{if(!i.classList.contains("callout-left-controls"))return!1
i=null}if(i&&!te(i)&&(!i.lastChild||i.lastChild.nodeType!==Node.ELEMENT_NODE||0!==a.textOffset))return!1
const r=l+2+n.child(0).content.size,c=t.state.tr.setSelection(e.create(t.state.doc,r))
return t.dispatch(c),!0}},view:()=>({update(t,e){const{selection:o,schema:n}=t.state
if(o.eq(e.selection))return
let l=null
if(o.node?.type===n.nodes.callout)l=o.from
else{const e=Te(t.state,n.nodes.callout)
e&&(l=e.pos)}r().appEvents.trigger("callout:selection-changed",l),t.dom.querySelectorAll(".composer-callout-node.has-selection").forEach(t=>t.classList.remove("has-selection")),null!==l&&t.nodeDOM(l)?.classList.add("has-selection")}})}),u=new t({key:new o("oneboxMarkerStrip"),appendTransaction(t,e,o){if(!t.some(t=>t.docChanged))return null
const n=o.schema.nodes.onebox
if(!n)return null
let l=null
return o.doc.descendants((t,e)=>{if(t.type!==n)return
const s=t.attrs.html
if(!s)return
const i=s.replace(se,"")
i!==s&&(l??=o.tr,l.setNodeMarkup(e,null,{...t.attrs,html:i}))}),l}})
return[c,d,u]}var oo=Object.freeze({__proto__:null,plugins:eo})
const no={callout:{group:"block",content:"callout_title callout_body",defining:!0,createGapCursor:!0,selectable:!0,draggable:!0,attrs:{type:{default:re},title:{default:""},fold:{default:""},isCollapsed:{default:!1},isCollapsible:{default:!1},hasBody:{default:!1},hasCustomTitle:{default:!1}},toDOM(t){const{type:e,fold:o,isCollapsed:n,isCollapsible:l}=t.attrs,s=["callout"]
return""!==o&&(n&&s.push("is-collapsed"),l&&s.push("is-collapsible")),["blockquote",{class:s.join(" "),"data-callout-type":e},0]},parseDOM:[{tag:"blockquote.callout",getAttrs(t){let e=t.getAttribute("data-callout-type")
const o=t.querySelector(".callout-title-inner")?.textContent.trim()||"",n=t.classList.contains("is-collapsible")?t.classList.contains("is-collapsed")?"-":"+":"",l=ue(e),s=l&&(l.title||l.label)||""
return{type:e,title:o,fold:n,hasCustomTitle:o.length>0&&o!==s}}}]},callout_title:{content:"inline*",defining:!0,selectable:!0,attrs:{type:{default:re},fold:{default:""},isCollapsed:{default:!1},isCollapsible:{default:!1},hasBody:{default:!1}},toDOM:()=>["div",{class:"callout-title"},["span",{class:"callout-title-inner"},0]],parseDOM:[{tag:"div.callout-title",contentElement:"span.callout-title-inner"}]},callout_body:{content:"block*",defining:!0,selectable:!1,createGapCursor:!0,toDOM:()=>["div",{class:"callout-content"},0],parseDOM:[{tag:"div.callout-content"}]},plugins:eo}
var lo=Object.freeze({__proto__:null,nodeSpec:no})
const so={name:"callout",nodeViews:$e,commands:Me,nodeSpec:no,parse:Ie,serializeNode:ze,inputRules:Le,plugins:eo}
var io=Object.freeze({__proto__:null,default:so})
const ao="aside.onebox, aside.quote[data-topic]",ro="a.onebox"
class co{static{dt7948.g(this.prototype,"calloutSettings",[s])}#t=void dt7948.i(this,"calloutSettings")
constructor(t,l){n(this,t),this.api=l,this.hasChatContext=!!l.decorateChatMessage,l.registerRichEditorExtension(so)
const s=window.I18n.fallbackLocale||"en"
window.I18n.translations[s].js.composer||(window.I18n.translations[s].js.composer={}),window.I18n.translations[s].js.composer.callout_sample="",l.addComposerToolbarPopupMenuOption({action:t=>{const e=re
t.commands?t.commands.insertCallout(e):t.applySurround(`> [!${e}]\n> `," ","callout_sample")},icon:"callout",label:B("composer.callout"),shortcut:"alt+C"}),l.modifyClass("component:modal/keyboard-shortcuts-help",t=>class extends t{get shortcuts(){const t=super.shortcuts
return t?.composing?.shortcuts?(t.composing.shortcuts.callout={shortcut:'\n              <span class="delimiter-or" dir="ltr">\n                <kbd>Ctrl</kbd>\n                <kbd>Alt</kbd>\n                <kbd>C</kbd>\n              </span>',shortcutTexts:["Ctrl Alt C"],description:d(B("composer.insert_callout"))},t):t}})
const i=this
l.modifyClass("component:modal/history",t=>class extends t{async calculateBodyDiff(t,[e]){if(await super.calculateBodyDiff(t,[e]),"side_by_side_markdown"===this.viewMode||!this.bodyDiff)return
const o=document.createElement("div")
o.innerHTML=this.bodyDiff,i.renderStaticCallouts(o,this.viewMode),this.bodyDiff=o.innerHTML}static{dt7948.n(this.prototype,"calculateBodyDiff",[e])}})
l.registerValueTransformer("topic-escaped-excerpt",({value:t})=>t?.replace(se,""))||l.modifyClass("model:topic",t=>class extends t{get escapedExcerpt(){return super.escapedExcerpt?.replace(se,"")}static{dt7948.n(this.prototype,"escapedExcerpt",[o("excerpt")])}}),l.modifyClass("component:post/cooked-html",t=>class extends t{get cooked(){const t=super.cooked
return this.args.cooked&&"post__contents-cooked-quote"===this.args.className&&t?t.toString().replace(se,""):t}}),l.decorateCookedElement((t,e)=>(this.processCookedElement(t,e),()=>this.disconnectPreviewObserver(t))),this.hasChatContext&&(l.decorateChatMessage((t,e)=>{this.processCookedElement(t,e,{isChat:!0})},{id:"quote-callouts"}),l.registerChatComposerButton?.({id:"quote-callouts",icon:"callout",label:B("composer.insert_callout"),position:"dropdown",action(){const t="callout-chooser",e=document.querySelector(".chat-composer-dropdown__trigger-btn")
this.menu.show(e,{identifier:t,component:D(I({id:null,block:'[[[8,[32,0],null,[["@onSelect","@close"],[[30,1,["onSelect"]],[30,1,["close"]]]],null]],["@data"],[]]',moduleName:"(unknown template module)",scope:()=>[ye],isStrictMode:!0}),R()),data:{onSelect:t=>{const e=`> [!${t}]\n> `
this.composer.textarea.addText(this.composer.textarea.getSelected(),e),this.composer.focus()},close:()=>{this.menu.close(t)}}})}}))}renderStaticCallouts(t,e){if("inline"!==e&&"side_by_side"!==e)return
for(const e of t.querySelectorAll("blockquote")){if(!e.parentElement)continue
let t,o=!1
if(e.classList.contains("diff-ins")||e.classList.contains("diff-del"))e.querySelectorAll("ins, del").forEach(t=>t.replaceWith(...t.childNodes)),e.normalize()
else{const n=e.firstElementChild
"P"===n?.tagName&&(n.innerHTML=n.innerHTML.replace(/\[!.*?\]/,e=>{if(/<ins[\s>]/.test(e)&&/<del[\s>]/.test(e)){o=!0
const n=e.replace(/<ins[^>]*>.*?<\/ins>/g,"").replace(/<\/?del[^>]*>/g,"").match(/^\[!([^\]]+)\]/)
return n&&(t=n[1].toLowerCase()),e.replace(/<del[^>]*>.*?<\/del>/g,"").replace(/<\/?ins[^>]*>/g,"")}return e.replace(/<\/?(?:ins|del)[^>]*>/g,"")}))}const n=["diff-ins","diff-del"].filter(t=>e.classList.contains(t)),l=this.parseHeaders(e)
if(l?.isCallout){o&&t&&(l.previousType=t)
const e=this.buildStaticCallout(l)
n.forEach(t=>e.classList.add(t)),o&&e.classList.add("callout-type-changed"),l.root.replaceWith(e)}}if("side_by_side"!==e)return
const o=e=>[...t.querySelectorAll(`${e} .callout`)].filter(t=>!t.classList.contains("diff-ins")&&!t.classList.contains("diff-del")),n=o(".revision-content.--previous"),l=o(".revision-content.--current"),s=Math.min(n.length,l.length)
for(let t=0;t<s;t++)n[t].dataset.calloutType!==l[t].dataset.calloutType&&(n[t].classList.add("callout-type-changed"),l[t].classList.add("callout-type-changed"))}buildStaticCallout(t){const e=this.calloutSettings.find(t.type),o=e?.mainType||e?.type||t.type,n=e?.type?t.type:o,l=e?.icon||P.callout_fallback_icon,s=e?.color||P.callout_fallback_color,r=document.createElement("blockquote")
r.className="callout",r.dataset.calloutType=o,r.dataset.calloutAlias=n,r.style.setProperty("--q-callout-color",s),r.style.setProperty("--q-callout-background",Qt(s,P.callout_background_opacity/100))
const c=document.createElement("div")
if(c.className="callout-title",t.previousType){const e=this.calloutSettings.find(t.previousType),o=e?.icon||P.callout_fallback_icon
if(o){const t=document.createElement("span")
t.className="callout-icon callout-icon--old",t.innerHTML=o.startsWith("<svg")?Vt(o):i(a(o)),c.append(t)}}if(l){const e=document.createElement("span")
e.className=t.previousType?"callout-icon callout-icon--new":"callout-icon",e.innerHTML=l.startsWith("<svg")?Vt(l):i(a(l)),c.append(e)}const d=document.createElement("span")
if(d.className="callout-title-inner",t.title.hasInline&&t.title.nodes.length?t.title.nodes.forEach(t=>d.append(t)):d.textContent=t.title.text||e?.title||Jt(o),c.append(d),r.append(c),t.children?.length){const e=document.createElement("div")
e.className="callout-content"
for(const o of t.children)o.isCallout?e.append(this.buildStaticCallout(o)):o.content&&e.append(o.content)
r.append(e)}return r}processCookedElement(t,e,{isChat:o=!1}={}){const n=!o&&!e.model,l={value:0}
t.querySelectorAll(ao).forEach(t=>this.stripMarkerFromExcerpt(t)),n&&this.observePreviewOneboxes(t)
for(const o of t.querySelectorAll("blockquote")){if(!o.parentElement)continue
const t=this.parseHeaders(o)
if(!t?.isCallout)continue
n&&this.assignPreviewMetadata(t,l)
const{root:s}=t,i=document.createElement("div")
s.replaceWith(i),e.renderGlimmer(i,Ce,{...t})}}stripMarkerFromExcerpt(t){const e=document.createTreeWalker(t,NodeFilter.SHOW_TEXT)
let o
for(;o=e.nextNode();)o.nodeValue=o.nodeValue.replace(se,"")}observePreviewOneboxes(t){if(this.previewObservers??=new WeakMap,this.previewObservers.has(t))return
if(!t.querySelector(ro))return
const e=new MutationObserver(e=>{for(const t of e)for(const e of t.addedNodes)e.nodeType===Node.ELEMENT_NODE&&e.matches(ao)&&this.stripMarkerFromExcerpt(e)
t.querySelector(ro)||this.disconnectPreviewObserver(t)})
e.observe(t,{childList:!0,subtree:!0}),this.previewObservers.set(t,e)}disconnectPreviewObserver(t){const e=this.previewObservers?.get(t)
e&&(e.disconnect(),this.previewObservers.delete(t))}parseHeaders(t){const e=t?.firstElementChild
if(!e||"P"!==e.tagName)return null
const o=ne(oe(e))
if(!o)return null
const n=o.match(ie)
if(!n)return null
const l=n.groups.callout.toLowerCase()||re,s=n.groups.fold||"",i=n.groups.title?.trim()||""
e.innerHTML=e.innerHTML.replace(n.groups.marker,"").trimLeft()
const{nodes:a,hasInline:r}=this.collectTitleNodes(e)
te(e)&&e.remove()
return{root:t,isCallout:!0,type:l,title:{text:i,nodes:a,hasInline:r},fold:s,children:Array.from(t.children).map(t=>{if("BLOCKQUOTE"===t.tagName){const e=this.parseHeaders(t)
if(e)return e}return{content:t,isCallout:!1}})}}assignPreviewMetadata(t,e){if(t.isPreview=!0,t.calloutIndex=e.value++,t.children)for(const o of t.children)o.isCallout&&this.assignPreviewMetadata(o,e)}collectTitleNodes(t){const e=ee(t,t=>"BR"===t.nodeName||t.nodeType===Node.TEXT_NODE&&t.textContent.startsWith("\n"),{onStop:t=>t.remove()}),o=e.some(t=>t.nodeType===Node.ELEMENT_NODE)
return e.forEach(t=>t.remove()),{nodes:e,hasInline:o}}}var uo={name:"discourse-quote-callouts",initialize(t){(function(...t){const e="string"==typeof t[0]?2:1
t[e]={...t[e],[r]:U},c(...t)})(e=>{this.instance=new co(t,e)})},teardown(){this.instance=null}},po=Object.freeze({__proto__:null,default:uo})
class ho extends l{static{dt7948.g(this.prototype,"calloutPos",[p],function(){return null})}#w=void dt7948.i(this,"calloutPos")
get isEnabled(){return null!==this.calloutPos}toggle(t){this.calloutPos=this.calloutPos===t?null:t}enable(t){this.calloutPos=t}reset(){this.calloutPos=null}isEnabledFor(t){return this.calloutPos===t}}var fo=Object.freeze({__proto__:null,default:ho})
const mo={"discourse/api-initializers/callouts":po,"discourse/components/callout-chooser-panel":be,"discourse/components/callout-chooser":_e,"discourse/components/callout-node-view":Be,"discourse/components/callout-title-node-view":Fe,"discourse/components/callout":we,"discourse/helpers/icon-or-svg":Kt,"discourse/lib/config":ge,"discourse/lib/glimmer-node-view":qe,"discourse/lib/rich-editor-extension/commands":Oe,"discourse/lib/rich-editor-extension/index":io,"discourse/lib/rich-editor-extension/input-rules":De,"discourse/lib/rich-editor-extension/keyboard":Xe,"discourse/lib/rich-editor-extension/markdown":Re,"discourse/lib/rich-editor-extension/node-views":je,"discourse/lib/rich-editor-extension/paste-handler":to,"discourse/lib/rich-editor-extension/plugins":oo,"discourse/lib/rich-editor-extension/schema":lo,"discourse/lib/rich-editor-utils":xe,"discourse/lib/svg":Yt,"discourse/lib/utils":le,"discourse/lib/vendor/dom-purify":Wt,"discourse/services/callout-move-state":fo,"discourse/services/callout-settings":Object.freeze({__proto__:null,default:class extends l{allTypes(){return fe()}all(){return pe()}chooser(){return he()}find(t){return ue(t)}search(t){return me(t)}}})}
export{mo as default}

//# sourceMappingURL=f329167ed225083f5c1d8fdae33a4aaebed8e694.map?__ws=www.nodeloc.com
