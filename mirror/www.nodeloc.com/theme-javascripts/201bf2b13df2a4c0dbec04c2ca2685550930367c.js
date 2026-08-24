const{getObjectForTheme:e}=window.moduleBroker.lookup("discourse/lib/theme-settings-store"),{_INTERNAL_SOURCE_KEY:t,apiInitializer:o}=window.moduleBroker.lookup("discourse/lib/api"),{wantsNewWindow:i}=window.moduleBroker.lookup("discourse/lib/intercept-click"),{default:r}=window.moduleBroker.lookup("@glimmer/component"),{action:l}=window.moduleBroker.lookup("@ember/object"),{default:n}=window.moduleBroker.lookup("@ember/render-modifiers/modifiers/did-insert"),{default:c}=window.moduleBroker.lookup("@ember/render-modifiers/modifiers/will-destroy"),{bind:s}=window.moduleBroker.lookup("discourse/lib/decorators"),{default:d}=window.moduleBroker.lookup("discourse/lib/url"),{setComponentTemplate:a}=window.moduleBroker.lookup("@ember/component"),{createTemplateFactory:u}=window.moduleBroker.lookup("@ember/template-factory")
e(35)
const m=Object.freeze({type:"theme",id:35})
var p=function(...e){const i="string"==typeof e[0]?2:1
return e[i]={...e[i],[t]:m},o(...e)}(e=>{e.registerBehaviorTransformer("topic-list-item-click",({context:t,next:o})=>{const r=t.event.target,l=t.topic,n=["topic-list-data","link-bottom-line","topic-list-item"]
if(e.container.lookup("service:site").mobileView&&n.push("topic-item-metadata","topic-item-stats"),n.some(e=>r.classList.contains(e)))return!!i(event)||t.navigateToTopic(l,l.lastUnreadUrl)
o()})}),k=Object.freeze({__proto__:null,default:p})
class w extends r{clickHandler(e){const t=e.target,o=this.args.outletArgs.topic
if("DIV"===t.tagName){if(i(e))return!0
d.routeTo(o.lastUnreadUrl||o.url)}}static{dt7948.n(this.prototype,"clickHandler",[s])}registerClickHandler(e){e.parentElement.addEventListener("click",this.clickHandler)}static{dt7948.n(this.prototype,"registerClickHandler",[l])}removeClickHandler(e){e.parentElement.removeEventListener("click",this.clickHandler)}static{dt7948.n(this.prototype,"removeClickHandler",[l])}static{a(u({id:null,block:'[[[11,0],[24,0,"hidden"],[4,[32,0],[[30,0,["registerClickHandler"]]],null],[4,[32,1],[[30,0,["removeClickHandler"]]],null],[12],[13]],[],[]]',moduleName:"(unknown template module)",scope:()=>[n,c],isStrictMode:!0}),this)}}const b={"discourse/api-initializers/clickable-topic-row":k,"discourse/connectors/above-latest-topic-list-item/clickable-topic-row":Object.freeze({__proto__:null,default:w})}
export{b as default}

//# sourceMappingURL=201bf2b13df2a4c0dbec04c2ca2685550930367c.map?__ws=www.nodeloc.com
