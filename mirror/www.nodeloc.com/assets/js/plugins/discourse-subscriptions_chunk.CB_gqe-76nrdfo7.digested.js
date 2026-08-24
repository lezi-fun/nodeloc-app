const{default:t,computed:e,action:o}=window.moduleBroker.lookup("@ember/object"),{ajax:n}=window.moduleBroker.lookup("discourse/lib/ajax"),{default:a}=window.moduleBroker.lookup("@glimmer/component"),{tracked:s}=window.moduleBroker.lookup("@glimmer/tracking"),{default:i,setComponentTemplate:r,Input:u}=window.moduleBroker.lookup("@ember/component"),{fn:d,hash:c}=window.moduleBroker.lookup("@ember/helper"),{default:l}=window.moduleBroker.lookup("discourse/ui-kit/d-button"),{default:p}=window.moduleBroker.lookup("discourse/ui-kit/d-modal"),{i18n:m}=window.moduleBroker.lookup("discourse-i18n"),{createTemplateFactory:h}=window.moduleBroker.lookup("@ember/template-factory"),{default:_}=window.moduleBroker.lookup("discourse/lib/get-url"),{_INTERNAL_SOURCE_KEY:b}=window.moduleBroker.lookup("discourse/lib/api"),{withPluginApi:f}=window.moduleBroker.lookup("discourse/lib/plugin-api"),{on:k}=window.moduleBroker.lookup("@ember/modifier"),{tagName:w}=window.moduleBroker.lookup("@ember-decorators/component"),{default:y}=window.moduleBroker.lookup("discourse/helpers/with-event-value"),{default:v}=window.moduleBroker.lookup("discourse/select-kit/components/combo-box"),{trustHTML:g}=window.moduleBroker.lookup("@ember/template"),{autoUpdatingRelativeAge:x}=window.moduleBroker.lookup("discourse/lib/formatter")
class B extends t{static list(){return n("/s/admin/coupons",{method:"get"}).then(t=>null===t?{unconfigured:!0}:t.map(t=>B.create(t)))}static save(t){const e={promo:t.promo,discount_type:t.discount_type,discount:t.discount,active:t.active}
return n("/s/admin/coupons",{method:"post",data:e}).then(t=>B.create(t))}static update(t){const e={id:t.id,active:t.active}
return n("/s/admin/coupons",{method:"put",data:e}).then(t=>B.create(t))}static destroy(t){const e={coupon_id:t.coupon.id}
return n("/s/admin/coupons",{method:"delete",data:e})}get discount(){return this.coupon?.amount_off?`${parseFloat(.01*this.coupon?.amount_off).toFixed(2)}`:this.coupon?.percent_off?`${this.coupon?.percent_off}%`:void 0}static{dt7948.n(this.prototype,"discount",[e("coupon.amount_off","coupon.percent_off")])}}var C=Object.freeze({__proto__:null,default:B})
class $ extends a{static{dt7948.g(this.prototype,"refund",[s])}#t=void dt7948.i(this,"refund")
static{r(h({id:null,block:'[[[8,[32,0],null,[["@title","@closeModal"],[[28,[32,1],["discourse_subscriptions.user.subscriptions.operations.destroy.confirm"],null],[30,1]]],[["body","footer"],[[[[1,"\\n    "],[8,[32,2],null,[["@type","@checked"],["checkbox",[30,0,["refund"]]]],null],[1,"\\n    "],[1,[28,[32,1],["discourse_subscriptions.admin.ask_refund"],null]],[1,"\\n  "]],[]],[[[1,"\\n    "],[8,[32,3],[[24,0,"btn-danger"]],[["@label","@action","@icon","@isLoading"],["yes_value",[28,[32,4],[[30,2,["cancelSubscription"]],[28,[32,5],null,[["subscription","refund","closeModal"],[[30,2,["subscription"]],[30,0,["refund"]],[30,1]]]]],null],"xmark",[30,2,["subscription","loading"]]]],null],[1,"\\n    "],[8,[32,3],null,[["@label","@action"],["no_value",[30,1]]],null],[1,"\\n  "]],[]]]]]],["@closeModal","@model"],[]]',moduleName:"(unknown template module)",scope:()=>[p,m,u,l,d,c],isStrictMode:!0}),this)}}var T=Object.freeze({__proto__:null,default:$})
class j extends t{static find(){return n("/s/admin/subscriptions",{method:"get"}).then(t=>null===t?{unconfigured:!0}:(t.data=t.data.map(t=>j.create(t)),t))}static loadMore(t){return n(`/s/admin/subscriptions?last_record=${t}`,{method:"get"}).then(t=>(t.data=t.data.map(t=>j.create(t)),t))}get canceled(){return"canceled"===this.status}static{dt7948.n(this.prototype,"canceled",[e("status")])}get metadataUserExists(){return this.metadata.user_id&&this.metadata.username}static{dt7948.n(this.prototype,"metadataUserExists",[e("metadata")])}get subscriptionUserPath(){return _(`/admin/users/${this.metadata.user_id}/${this.metadata.username}`)}static{dt7948.n(this.prototype,"subscriptionUserPath",[e("metadata")])}destroy(t){const e={refund:t}
return n(`/s/admin/subscriptions/${this.id}`,{method:"delete",data:e}).then(t=>j.create(t))}}var D=Object.freeze({__proto__:null,default:j})
const z=Object.freeze({type:"plugin",name:"discourse-subscriptions"})
function N(...t){const e="string"==typeof t[0]?2:1
return t[e]={...t[e],[b]:z},f(...t)}class O extends t{static findAll(){return n("/s/admin/products",{method:"get"}).then(t=>null===t?{unconfigured:!0}:t.map(t=>O.create(t)))}static find(t){return n(`/s/admin/products/${t}`,{method:"get"}).then(t=>O.create(t))}isNew=!1
metadata={}
destroy(){return n(`/s/admin/products/${this.id}`,{method:"delete"})}save(){const t={name:this.name,statement_descriptor:this.statement_descriptor,metadata:this.metadata,active:this.active}
return n("/s/admin/products",{method:"post",data:t}).then(t=>O.create(t))}update(){const t={name:this.name,statement_descriptor:this.statement_descriptor,metadata:this.metadata,active:this.active}
return n(`/s/admin/products/${this.id}`,{method:"patch",data:t})}}var P=Object.freeze({__proto__:null,default:O})
class R extends t{get amountDollars(){return parseFloat(this.get("unit_amount")/100).toFixed(2)}static{dt7948.n(this.prototype,"amountDollars",[e("unit_amount")])}set amountDollars(t){const e=Math.round(100*parseFloat(t))
this.set("unit_amount",e)}get billingInterval(){return this.recurring?.interval||"one-time"}static{dt7948.n(this.prototype,"billingInterval",[e("recurring.interval")])}get subscriptionRate(){return`${this.amountDollars} ${this.currency.toUpperCase()} / ${this.billingInterval}`}static{dt7948.n(this.prototype,"subscriptionRate",[e("amountDollars","currency","billingInterval")])}}var F=Object.freeze({__proto__:null,default:R})
class A extends R{static findAll(t){return n("/s/admin/plans",{method:"get",data:t}).then(t=>t.map(t=>A.create(t)))}static find(t){return n(`/s/admin/plans/${t}`,{method:"get"}).then(t=>A.create(t))}isNew=!1
name=""
interval="month"
unit_amount=0
intervals=["day","week","month","year"]
metadata={}
get parseTrialPeriodDays(){return this.trial_period_days?parseInt(0+this.trial_period_days,10):0}static{dt7948.n(this.prototype,"parseTrialPeriodDays",[e("trial_period_days")])}save(){const t={nickname:this.nickname,interval:this.interval,amount:this.unit_amount,currency:this.currency,trial_period_days:this.parseTrialPeriodDays,type:this.type,product:this.product,metadata:this.metadata,active:this.active}
return n("/s/admin/plans",{method:"post",data:t})}update(){const t={nickname:this.nickname,trial_period_days:this.parseTrialPeriodDays,metadata:this.metadata,active:this.active}
return n(`/s/admin/plans/${this.id}`,{method:"patch",data:t})}}var M=Object.freeze({__proto__:null,default:A})
const U=dt7948.c(class extends i{discountType="amount"
discount=null
promoCode=null
active=!1
get discountTypes(){return[{id:"amount",name:"Amount"},{id:"percent",name:"Percent"}]}static{dt7948.n(this.prototype,"discountTypes",[e])}createNewCoupon(){const t={promo:this.promoCode,discount_type:this.discountType,discount:this.discount,active:this.active}
this.create(t)}static{dt7948.n(this.prototype,"createNewCoupon",[o])}cancelCreate(){this.cancel()}static{dt7948.n(this.prototype,"cancelCreate",[o])}static{r(h({id:null,block:'[[[11,0],[24,0,"create-coupon-form"],[17,1],[12],[1,"\\n  "],[10,"form"],[14,0,"form-horizontal"],[12],[1,"\\n    "],[10,2],[12],[1,"\\n      "],[10,"label"],[14,"for","promo_code"],[12],[1,"\\n        "],[1,[28,[32,0],["discourse_subscriptions.admin.coupons.promo_code"],null]],[1,"\\n      "],[13],[1,"\\n      "],[11,"input"],[24,3,"promo_code"],[16,2,[30,0,["promoCode"]]],[24,4,"text"],[4,[32,1],["input",[28,[32,2],[[28,[32,3],[[28,[31,0],[[30,0,["promoCode"]]],null]],null]],null]],null],[12],[13],[1,"\\n    "],[13],[1,"\\n\\n    "],[10,2],[12],[1,"\\n      "],[10,"label"],[14,"for","amount"],[12],[1,"\\n        "],[1,[28,[32,0],["discourse_subscriptions.admin.coupons.discount"],null]],[1,"\\n      "],[13],[1,"\\n      "],[8,[32,4],null,[["@content","@value","@onChange"],[[30,0,["discountTypes"]],[30,0,["discountType"]],[28,[32,3],[[28,[31,0],[[30,0,["discountType"]]],null]],null]]],null],[1,"\\n      "],[11,"input"],[24,0,"discount-amount"],[24,3,"amount"],[16,2,[30,0,["discount"]]],[24,4,"text"],[4,[32,1],["input",[28,[32,2],[[28,[32,3],[[28,[31,0],[[30,0,["discount"]]],null]],null]],null]],null],[12],[13],[1,"\\n    "],[13],[1,"\\n\\n    "],[10,2],[12],[1,"\\n      "],[10,"label"],[14,"for","active"],[12],[1,"\\n        "],[1,[28,[32,0],["discourse_subscriptions.admin.coupons.active"],null]],[1,"\\n      "],[13],[1,"\\n      "],[8,[32,5],[[24,3,"active"]],[["@type","@checked"],["checkbox",[30,0,["active"]]]],null],[1,"\\n    "],[13],[1,"\\n  "],[13],[1,"\\n\\n  "],[8,[32,6],[[24,0,"btn-primary btn btn-icon"]],[["@action","@label","@title","@icon"],[[30,0,["createNewCoupon"]],"discourse_subscriptions.admin.coupons.create","discourse_subscriptions.admin.coupons.create","plus"]],null],[1,"\\n\\n  "],[8,[32,6],[[24,"label","cancel"],[24,0,"btn btn-icon"]],[["@action","@title","@icon"],[[30,0,["cancelCreate"]],"cancel","xmark"]],null],[1,"\\n"],[13]],["&attrs"],["mut"]]',moduleName:"(unknown template module)",scope:()=>[m,k,y,d,v,u,l],isStrictMode:!0}),this)}},[w("")])
var I=Object.freeze({__proto__:null,default:U})
function K(t){if(t){const e=new Date(moment.unix(t).format())
return new g(x(e,{format:"medium",title:!0,leaveAgo:!0}))}}var E=Object.freeze({__proto__:null,default:K})
function S(t,e){let o
switch(t.toUpperCase()){case"EUR":o="€"
break
case"GBP":o="£"
break
case"INR":o="₹"
break
case"BRL":o="R$"
break
case"DKK":o="DKK"
break
case"SGD":o="S$"
break
case"ZAR":o="R"
break
case"CHF":o="CHF"
break
case"PLN":o="zł"
break
case"CZK":o="Kč"
break
case"SEK":o="kr"
break
default:o="$"}return o+parseFloat(e).toFixed(2)}var L=Object.freeze({__proto__:null,default:S})
export{B as A,U as C,F as M,R as P,$ as a,j as b,O as c,A as d,S as e,K as f,D as g,P as h,M as i,C as j,E as k,L as l,T as m,I as n,N as w}

//# sourceMappingURL=../../map/plugins/discourse-subscriptions_chunk.CB_gqe-76nrdfo7.digested.js.map
