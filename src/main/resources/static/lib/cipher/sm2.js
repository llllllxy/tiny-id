/**
 * Skipped minification because the original files appears to be already minified.
 * Original file: /npm/sm-crypto@0.3.13/dist/sm2.js
 *
 * Do NOT use SRI with dynamically generated files! More information: https://www.jsdelivr.com/using-sri-with-dynamic-files
 */
!function(t,r){"object"==typeof exports&&"object"==typeof module?module.exports=r():"function"==typeof define&&define.amd?define([],r):"object"==typeof exports?exports.sm2=r():t.sm2=r()}("undefined"!=typeof self?self:this,function(){return function(t){function r(e){if(i[e])return i[e].exports;var n=i[e]={i:e,l:!1,exports:{}};return t[e].call(n.exports,n,n.exports,r),n.l=!0,n.exports}var i={};return r.m=t,r.c=i,r.d=function(t,i,e){r.o(t,i)||Object.defineProperty(t,i,{configurable:!1,enumerable:!0,get:e})},r.n=function(t){var i=t&&t.__esModule?function(){return t.default}:function(){return t};return r.d(i,"a",i),i},r.o=function(t,r){return Object.prototype.hasOwnProperty.call(t,r)},r.p="",r(r.s=2)}([function(t,r,i){(function(){function i(t,r,i){null!=t&&("number"==typeof t?this.fromNumber(t,r,i):null==r&&"string"!=typeof t?this.fromString(t,256):this.fromString(t,r))}function e(){return new i(null)}function n(t,r,i,e,n,o){for(;--o>=0;){var s=r*this[t++]+i[e]+n;n=Math.floor(s/67108864),i[e++]=67108863&s}return n}function o(t,r,i,e,n,o){for(var s=32767&r,u=r>>15;--o>=0;){var h=32767&this[t],a=this[t++]>>15,f=u*h+a*s;h=s*h+((32767&f)<<15)+i[e]+(1073741823&n),n=(h>>>30)+(f>>>15)+u*a+(n>>>30),i[e++]=1073741823&h}return n}function s(t,r,i,e,n,o){for(var s=16383&r,u=r>>14;--o>=0;){var h=16383&this[t],a=this[t++]>>14,f=u*h+a*s;h=s*h+((16383&f)<<14)+i[e]+n,n=(h>>28)+(f>>14)+u*a,i[e++]=268435455&h}return n}function u(t){return pr.charAt(t)}function h(t,r){var i=vr[t.charCodeAt(r)];return null==i?-1:i}function a(t){for(var r=this.t-1;r>=0;--r)t[r]=this[r];t.t=this.t,t.s=this.s}function f(t){this.t=1,this.s=t<0?-1:0,t>0?this[0]=t:t<-1?this[0]=t+this.DV:this.t=0}function l(t){var r=e();return r.fromInt(t),r}function c(t,r){var e;if(16==r)e=4;else if(8==r)e=3;else if(256==r)e=8;else if(2==r)e=1;else if(32==r)e=5;else{if(4!=r)return void this.fromRadix(t,r);e=2}this.t=0,this.s=0;for(var n=t.length,o=!1,s=0;--n>=0;){var u=8==e?255&t[n]:h(t,n);u<0?"-"==t.charAt(n)&&(o=!0):(o=!1,0==s?this[this.t++]=u:s+e>this.DB?(this[this.t-1]|=(u&(1<<this.DB-s)-1)<<s,this[this.t++]=u>>this.DB-s):this[this.t-1]|=u<<s,(s+=e)>=this.DB&&(s-=this.DB))}8==e&&0!=(128&t[0])&&(this.s=-1,s>0&&(this[this.t-1]|=(1<<this.DB-s)-1<<s)),this.clamp(),o&&i.ZERO.subTo(this,this)}function p(){for(var t=this.s&this.DM;this.t>0&&this[this.t-1]==t;)--this.t}function v(t){if(this.s<0)return"-"+this.negate().toString(t);var r;if(16==t)r=4;else if(8==t)r=3;else if(2==t)r=1;else if(32==t)r=5;else{if(4!=t)return this.toRadix(t);r=2}var i,e=(1<<r)-1,n=!1,o="",s=this.t,h=this.DB-s*this.DB%r;if(s-- >0)for(h<this.DB&&(i=this[s]>>h)>0&&(n=!0,o=u(i));s>=0;)h<r?(i=(this[s]&(1<<h)-1)<<r-h,i|=this[--s]>>(h+=this.DB-r)):(i=this[s]>>(h-=r)&e,h<=0&&(h+=this.DB,--s)),i>0&&(n=!0),n&&(o+=u(i));return n?o:"0"}function y(){var t=e();return i.ZERO.subTo(this,t),t}function m(){return this.s<0?this.negate():this}function d(t){var r=this.s-t.s;if(0!=r)return r;var i=this.t;if(0!=(r=i-t.t))return this.s<0?-r:r;for(;--i>=0;)if(0!=(r=this[i]-t[i]))return r;return 0}function g(t){var r,i=1;return 0!=(r=t>>>16)&&(t=r,i+=16),0!=(r=t>>8)&&(t=r,i+=8),0!=(r=t>>4)&&(t=r,i+=4),0!=(r=t>>2)&&(t=r,i+=2),0!=(r=t>>1)&&(t=r,i+=1),i}function T(){return this.t<=0?0:this.DB*(this.t-1)+g(this[this.t-1]^this.s&this.DM)}function F(t,r){var i;for(i=this.t-1;i>=0;--i)r[i+t]=this[i];for(i=t-1;i>=0;--i)r[i]=0;r.t=this.t+t,r.s=this.s}function b(t,r){for(var i=t;i<this.t;++i)r[i-t]=this[i];r.t=Math.max(this.t-t,0),r.s=this.s}function w(t,r){var i,e=t%this.DB,n=this.DB-e,o=(1<<n)-1,s=Math.floor(t/this.DB),u=this.s<<e&this.DM;for(i=this.t-1;i>=0;--i)r[i+s+1]=this[i]>>n|u,u=(this[i]&o)<<e;for(i=s-1;i>=0;--i)r[i]=0;r[s]=u,r.t=this.t+s+1,r.s=this.s,r.clamp()}function B(t,r){r.s=this.s;var i=Math.floor(t/this.DB);if(i>=this.t)return void(r.t=0);var e=t%this.DB,n=this.DB-e,o=(1<<e)-1;r[0]=this[i]>>e;for(var s=i+1;s<this.t;++s)r[s-i-1]|=(this[s]&o)<<n,r[s-i]=this[s]>>e;e>0&&(r[this.t-i-1]|=(this.s&o)<<n),r.t=this.t-i,r.clamp()}function x(t,r){for(var i=0,e=0,n=Math.min(t.t,this.t);i<n;)e+=this[i]-t[i],r[i++]=e&this.DM,e>>=this.DB;if(t.t<this.t){for(e-=t.s;i<this.t;)e+=this[i],r[i++]=e&this.DM,e>>=this.DB;e+=this.s}else{for(e+=this.s;i<t.t;)e-=t[i],r[i++]=e&this.DM,e>>=this.DB;e-=t.s}r.s=e<0?-1:0,e<-1?r[i++]=this.DV+e:e>0&&(r[i++]=e),r.t=i,r.clamp()}function D(t,r){var e=this.abs(),n=t.abs(),o=e.t;for(r.t=o+n.t;--o>=0;)r[o]=0;for(o=0;o<n.t;++o)r[o+e.t]=e.am(0,n[o],r,o,0,e.t);r.s=0,r.clamp(),this.s!=t.s&&i.ZERO.subTo(r,r)}function I(t){for(var r=this.abs(),i=t.t=2*r.t;--i>=0;)t[i]=0;for(i=0;i<r.t-1;++i){var e=r.am(i,r[i],t,2*i,0,1);(t[i+r.t]+=r.am(i+1,2*r[i],t,2*i+1,e,r.t-i-1))>=r.DV&&(t[i+r.t]-=r.DV,t[i+r.t+1]=1)}t.t>0&&(t[t.t-1]+=r.am(i,r[i],t,2*i,0,1)),t.s=0,t.clamp()}function q(t,r,n){var o=t.abs();if(!(o.t<=0)){var s=this.abs();if(s.t<o.t)return null!=r&&r.fromInt(0),void(null!=n&&this.copyTo(n));null==n&&(n=e());var u=e(),h=this.s,a=t.s,f=this.DB-g(o[o.t-1]);f>0?(o.lShiftTo(f,u),s.lShiftTo(f,n)):(o.copyTo(u),s.copyTo(n));var l=u.t,c=u[l-1];if(0!=c){var p=c*(1<<this.F1)+(l>1?u[l-2]>>this.F2:0),v=this.FV/p,y=(1<<this.F1)/p,m=1<<this.F2,d=n.t,T=d-l,F=null==r?e():r;for(u.dlShiftTo(T,F),n.compareTo(F)>=0&&(n[n.t++]=1,n.subTo(F,n)),i.ONE.dlShiftTo(l,F),F.subTo(u,u);u.t<l;)u[u.t++]=0;for(;--T>=0;){var b=n[--d]==c?this.DM:Math.floor(n[d]*v+(n[d-1]+m)*y);if((n[d]+=u.am(0,b,n,T,0,l))<b)for(u.dlShiftTo(T,F),n.subTo(F,n);n[d]<--b;)n.subTo(F,n)}null!=r&&(n.drShiftTo(l,r),h!=a&&i.ZERO.subTo(r,r)),n.t=l,n.clamp(),f>0&&n.rShiftTo(f,n),h<0&&i.ZERO.subTo(n,n)}}}function S(t){var r=e();return this.abs().divRemTo(t,null,r),this.s<0&&r.compareTo(i.ZERO)>0&&t.subTo(r,r),r}function E(t){this.m=t}function A(t){return t.s<0||t.compareTo(this.m)>=0?t.mod(this.m):t}function P(t){return t}function O(t){t.divRemTo(this.m,null,t)}function R(t,r,i){t.multiplyTo(r,i),this.reduce(i)}function M(t,r){t.squareTo(r),this.reduce(r)}function C(){if(this.t<1)return 0;var t=this[0];if(0==(1&t))return 0;var r=3&t;return r=r*(2-(15&t)*r)&15,r=r*(2-(255&t)*r)&255,r=r*(2-((65535&t)*r&65535))&65535,r=r*(2-t*r%this.DV)%this.DV,r>0?this.DV-r:-r}function H(t){this.m=t,this.mp=t.invDigit(),this.mpl=32767&this.mp,this.mph=this.mp>>15,this.um=(1<<t.DB-15)-1,this.mt2=2*t.t}function k(t){var r=e();return t.abs().dlShiftTo(this.m.t,r),r.divRemTo(this.m,null,r),t.s<0&&r.compareTo(i.ZERO)>0&&this.m.subTo(r,r),r}function V(t){var r=e();return t.copyTo(r),this.reduce(r),r}function N(t){for(;t.t<=this.mt2;)t[t.t++]=0;for(var r=0;r<this.m.t;++r){var i=32767&t[r],e=i*this.mpl+((i*this.mph+(t[r]>>15)*this.mpl&this.um)<<15)&t.DM;for(i=r+this.m.t,t[i]+=this.m.am(0,e,t,r,0,this.m.t);t[i]>=t.DV;)t[i]-=t.DV,t[++i]++}t.clamp(),t.drShiftTo(this.m.t,t),t.compareTo(this.m)>=0&&t.subTo(this.m,t)}function j(t,r){t.squareTo(r),this.reduce(r)}function L(t,r,i){t.multiplyTo(r,i),this.reduce(i)}function z(){return 0==(this.t>0?1&this[0]:this.s)}function K(t,r){if(t>4294967295||t<1)return i.ONE;var n=e(),o=e(),s=r.convert(this),u=g(t)-1;for(s.copyTo(n);--u>=0;)if(r.sqrTo(n,o),(t&1<<u)>0)r.mulTo(o,s,n);else{var h=n;n=o,o=h}return r.revert(n)}function Z(t,r){var i;return i=t<256||r.isEven()?new E(r):new H(r),this.exp(t,i)}function U(){var t=e();return this.copyTo(t),t}function _(){if(this.s<0){if(1==this.t)return this[0]-this.DV;if(0==this.t)return-1}else{if(1==this.t)return this[0];if(0==this.t)return 0}return(this[1]&(1<<32-this.DB)-1)<<this.DB|this[0]}function X(){return 0==this.t?this.s:this[0]<<24>>24}function Y(){return 0==this.t?this.s:this[0]<<16>>16}function G(t){return Math.floor(Math.LN2*this.DB/Math.log(t))}function J(){return this.s<0?-1:this.t<=0||1==this.t&&this[0]<=0?0:1}function Q(t){if(null==t&&(t=10),0==this.signum()||t<2||t>36)return"0";var r=this.chunkSize(t),i=Math.pow(t,r),n=l(i),o=e(),s=e(),u="";for(this.divRemTo(n,o,s);o.signum()>0;)u=(i+s.intValue()).toString(t).substr(1)+u,o.divRemTo(n,o,s);return s.intValue().toString(t)+u}function W(t,r){this.fromInt(0),null==r&&(r=10);for(var e=this.chunkSize(r),n=Math.pow(r,e),o=!1,s=0,u=0,a=0;a<t.length;++a){var f=h(t,a);f<0?"-"==t.charAt(a)&&0==this.signum()&&(o=!0):(u=r*u+f,++s>=e&&(this.dMultiply(n),this.dAddOffset(u,0),s=0,u=0))}s>0&&(this.dMultiply(Math.pow(r,s)),this.dAddOffset(u,0)),o&&i.ZERO.subTo(this,this)}function $(t,r,e){if("number"==typeof r)if(t<2)this.fromInt(1);else for(this.fromNumber(t,e),this.testBit(t-1)||this.bitwiseTo(i.ONE.shiftLeft(t-1),ut,this),this.isEven()&&this.dAddOffset(1,0);!this.isProbablePrime(r);)this.dAddOffset(2,0),this.bitLength()>t&&this.subTo(i.ONE.shiftLeft(t-1),this);else{var n=new Array,o=7&t;n.length=1+(t>>3),r.nextBytes(n),o>0?n[0]&=(1<<o)-1:n[0]=0,this.fromString(n,256)}}function tt(){var t=this.t,r=new Array;r[0]=this.s;var i,e=this.DB-t*this.DB%8,n=0;if(t-- >0)for(e<this.DB&&(i=this[t]>>e)!=(this.s&this.DM)>>e&&(r[n++]=i|this.s<<this.DB-e);t>=0;)e<8?(i=(this[t]&(1<<e)-1)<<8-e,i|=this[--t]>>(e+=this.DB-8)):(i=this[t]>>(e-=8)&255,e<=0&&(e+=this.DB,--t)),0!=(128&i)&&(i|=-256),0==n&&(128&this.s)!=(128&i)&&++n,(n>0||i!=this.s)&&(r[n++]=i);return r}function rt(t){return 0==this.compareTo(t)}function it(t){return this.compareTo(t)<0?this:t}function et(t){return this.compareTo(t)>0?this:t}function nt(t,r,i){var e,n,o=Math.min(t.t,this.t);for(e=0;e<o;++e)i[e]=r(this[e],t[e]);if(t.t<this.t){for(n=t.s&this.DM,e=o;e<this.t;++e)i[e]=r(this[e],n);i.t=this.t}else{for(n=this.s&this.DM,e=o;e<t.t;++e)i[e]=r(n,t[e]);i.t=t.t}i.s=r(this.s,t.s),i.clamp()}function ot(t,r){return t&r}function st(t){var r=e();return this.bitwiseTo(t,ot,r),r}function ut(t,r){return t|r}function ht(t){var r=e();return this.bitwiseTo(t,ut,r),r}function at(t,r){return t^r}function ft(t){var r=e();return this.bitwiseTo(t,at,r),r}function lt(t,r){return t&~r}function ct(t){var r=e();return this.bitwiseTo(t,lt,r),r}function pt(){for(var t=e(),r=0;r<this.t;++r)t[r]=this.DM&~this[r];return t.t=this.t,t.s=~this.s,t}function vt(t){var r=e();return t<0?this.rShiftTo(-t,r):this.lShiftTo(t,r),r}function yt(t){var r=e();return t<0?this.lShiftTo(-t,r):this.rShiftTo(t,r),r}function mt(t){if(0==t)return-1;var r=0;return 0==(65535&t)&&(t>>=16,r+=16),0==(255&t)&&(t>>=8,r+=8),0==(15&t)&&(t>>=4,r+=4),0==(3&t)&&(t>>=2,r+=2),0==(1&t)&&++r,r}function dt(){for(var t=0;t<this.t;++t)if(0!=this[t])return t*this.DB+mt(this[t]);return this.s<0?this.t*this.DB:-1}function gt(t){for(var r=0;0!=t;)t&=t-1,++r;return r}function Tt(){for(var t=0,r=this.s&this.DM,i=0;i<this.t;++i)t+=gt(this[i]^r);return t}function Ft(t){var r=Math.floor(t/this.DB);return r>=this.t?0!=this.s:0!=(this[r]&1<<t%this.DB)}function bt(t,r){var e=i.ONE.shiftLeft(t);return this.bitwiseTo(e,r,e),e}function wt(t){return this.changeBit(t,ut)}function Bt(t){return this.changeBit(t,lt)}function xt(t){return this.changeBit(t,at)}function Dt(t,r){for(var i=0,e=0,n=Math.min(t.t,this.t);i<n;)e+=this[i]+t[i],r[i++]=e&this.DM,e>>=this.DB;if(t.t<this.t){for(e+=t.s;i<this.t;)e+=this[i],r[i++]=e&this.DM,e>>=this.DB;e+=this.s}else{for(e+=this.s;i<t.t;)e+=t[i],r[i++]=e&this.DM,e>>=this.DB;e+=t.s}r.s=e<0?-1:0,e>0?r[i++]=e:e<-1&&(r[i++]=this.DV+e),r.t=i,r.clamp()}function It(t){var r=e();return this.addTo(t,r),r}function qt(t){var r=e();return this.subTo(t,r),r}function St(t){var r=e();return this.multiplyTo(t,r),r}function Et(){var t=e();return this.squareTo(t),t}function At(t){var r=e();return this.divRemTo(t,r,null),r}function Pt(t){var r=e();return this.divRemTo(t,null,r),r}function Ot(t){var r=e(),i=e();return this.divRemTo(t,r,i),new Array(r,i)}function Rt(t){this[this.t]=this.am(0,t-1,this,0,0,this.t),++this.t,this.clamp()}function Mt(t,r){if(0!=t){for(;this.t<=r;)this[this.t++]=0;for(this[r]+=t;this[r]>=this.DV;)this[r]-=this.DV,++r>=this.t&&(this[this.t++]=0),++this[r]}}function Ct(){}function Ht(t){return t}function kt(t,r,i){t.multiplyTo(r,i)}function Vt(t,r){t.squareTo(r)}function Nt(t){return this.exp(t,new Ct)}function jt(t,r,i){var e=Math.min(this.t+t.t,r);for(i.s=0,i.t=e;e>0;)i[--e]=0;var n;for(n=i.t-this.t;e<n;++e)i[e+this.t]=this.am(0,t[e],i,e,0,this.t);for(n=Math.min(t.t,r);e<n;++e)this.am(0,t[e],i,e,0,r-e);i.clamp()}function Lt(t,r,i){--r;var e=i.t=this.t+t.t-r;for(i.s=0;--e>=0;)i[e]=0;for(e=Math.max(r-this.t,0);e<t.t;++e)i[this.t+e-r]=this.am(r-e,t[e],i,0,0,this.t+e-r);i.clamp(),i.drShiftTo(1,i)}function zt(t){this.r2=e(),this.q3=e(),i.ONE.dlShiftTo(2*t.t,this.r2),this.mu=this.r2.divide(t),this.m=t}function Kt(t){if(t.s<0||t.t>2*this.m.t)return t.mod(this.m);if(t.compareTo(this.m)<0)return t;var r=e();return t.copyTo(r),this.reduce(r),r}function Zt(t){return t}function Ut(t){for(t.drShiftTo(this.m.t-1,this.r2),t.t>this.m.t+1&&(t.t=this.m.t+1,t.clamp()),this.mu.multiplyUpperTo(this.r2,this.m.t+1,this.q3),this.m.multiplyLowerTo(this.q3,this.m.t+1,this.r2);t.compareTo(this.r2)<0;)t.dAddOffset(1,this.m.t+1);for(t.subTo(this.r2,t);t.compareTo(this.m)>=0;)t.subTo(this.m,t)}function _t(t,r){t.squareTo(r),this.reduce(r)}function Xt(t,r,i){t.multiplyTo(r,i),this.reduce(i)}function Yt(t,r){var i,n,o=t.bitLength(),s=l(1);if(o<=0)return s;i=o<18?1:o<48?3:o<144?4:o<768?5:6,n=o<8?new E(r):r.isEven()?new zt(r):new H(r);var u=new Array,h=3,a=i-1,f=(1<<i)-1;if(u[1]=n.convert(this),i>1){var c=e();for(n.sqrTo(u[1],c);h<=f;)u[h]=e(),n.mulTo(c,u[h-2],u[h]),h+=2}var p,v,y=t.t-1,m=!0,d=e();for(o=g(t[y])-1;y>=0;){for(o>=a?p=t[y]>>o-a&f:(p=(t[y]&(1<<o+1)-1)<<a-o,y>0&&(p|=t[y-1]>>this.DB+o-a)),h=i;0==(1&p);)p>>=1,--h;if((o-=h)<0&&(o+=this.DB,--y),m)u[p].copyTo(s),m=!1;else{for(;h>1;)n.sqrTo(s,d),n.sqrTo(d,s),h-=2;h>0?n.sqrTo(s,d):(v=s,s=d,d=v),n.mulTo(d,u[p],s)}for(;y>=0&&0==(t[y]&1<<o);)n.sqrTo(s,d),v=s,s=d,d=v,--o<0&&(o=this.DB-1,--y)}return n.revert(s)}function Gt(t){var r=this.s<0?this.negate():this.clone(),i=t.s<0?t.negate():t.clone();if(r.compareTo(i)<0){var e=r;r=i,i=e}var n=r.getLowestSetBit(),o=i.getLowestSetBit();if(o<0)return r;for(n<o&&(o=n),o>0&&(r.rShiftTo(o,r),i.rShiftTo(o,i));r.signum()>0;)(n=r.getLowestSetBit())>0&&r.rShiftTo(n,r),(n=i.getLowestSetBit())>0&&i.rShiftTo(n,i),r.compareTo(i)>=0?(r.subTo(i,r),r.rShiftTo(1,r)):(i.subTo(r,i),i.rShiftTo(1,i));return o>0&&i.lShiftTo(o,i),i}function Jt(t){if(t<=0)return 0;var r=this.DV%t,i=this.s<0?t-1:0;if(this.t>0)if(0==r)i=this[0]%t;else for(var e=this.t-1;e>=0;--e)i=(r*i+this[e])%t;return i}function Qt(t){var r=t.isEven();if(this.isEven()&&r||0==t.signum())return i.ZERO;for(var e=t.clone(),n=this.clone(),o=l(1),s=l(0),u=l(0),h=l(1);0!=e.signum();){for(;e.isEven();)e.rShiftTo(1,e),r?(o.isEven()&&s.isEven()||(o.addTo(this,o),s.subTo(t,s)),o.rShiftTo(1,o)):s.isEven()||s.subTo(t,s),s.rShiftTo(1,s);for(;n.isEven();)n.rShiftTo(1,n),r?(u.isEven()&&h.isEven()||(u.addTo(this,u),h.subTo(t,h)),u.rShiftTo(1,u)):h.isEven()||h.subTo(t,h),h.rShiftTo(1,h);e.compareTo(n)>=0?(e.subTo(n,e),r&&o.subTo(u,o),s.subTo(h,s)):(n.subTo(e,n),r&&u.subTo(o,u),h.subTo(s,h))}return 0!=n.compareTo(i.ONE)?i.ZERO:h.compareTo(t)>=0?h.subtract(t):h.signum()<0?(h.addTo(t,h),h.signum()<0?h.add(t):h):h}function Wt(t){var r,i=this.abs();if(1==i.t&&i[0]<=yr[yr.length-1]){for(r=0;r<yr.length;++r)if(i[0]==yr[r])return!0;return!1}if(i.isEven())return!1;for(r=1;r<yr.length;){for(var e=yr[r],n=r+1;n<yr.length&&e<mr;)e*=yr[n++];for(e=i.modInt(e);r<n;)if(e%yr[r++]==0)return!1}return i.millerRabin(t)}function $t(t){var r=this.subtract(i.ONE),n=r.getLowestSetBit();if(n<=0)return!1;var o=r.shiftRight(n);(t=t+1>>1)>yr.length&&(t=yr.length);for(var s=e(),u=0;u<t;++u){s.fromInt(yr[Math.floor(Math.random()*yr.length)]);var h=s.modPow(o,this);if(0!=h.compareTo(i.ONE)&&0!=h.compareTo(r)){for(var a=1;a++<n&&0!=h.compareTo(r);)if(h=h.modPowInt(2,this),0==h.compareTo(i.ONE))return!1;if(0!=h.compareTo(r))return!1}}return!0}function tr(t){gr[Tr++]^=255&t,gr[Tr++]^=t>>8&255,gr[Tr++]^=t>>16&255,gr[Tr++]^=t>>24&255,Tr>=Br&&(Tr-=Br)}function rr(){tr((new Date).getTime())}function ir(){if(null==dr){for(rr(),dr=hr(),dr.init(gr),Tr=0;Tr<gr.length;++Tr)gr[Tr]=0;Tr=0}return dr.next()}function er(t){var r;for(r=0;r<t.length;++r)t[r]=ir()}function nr(){}function or(){this.i=0,this.j=0,this.S=new Array}function sr(t){var r,i,e;for(r=0;r<256;++r)this.S[r]=r;for(i=0,r=0;r<256;++r)i=i+this.S[r]+t[r%t.length]&255,e=this.S[r],this.S[r]=this.S[i],this.S[i]=e;this.i=0,this.j=0}function ur(){var t;return this.i=this.i+1&255,this.j=this.j+this.S[this.i]&255,t=this.S[this.i],this.S[this.i]=this.S[this.j],this.S[this.j]=t,this.S[t+this.S[this.i]&255]}function hr(){return new or}var ar,fr="undefined"!=typeof navigator;fr&&"Microsoft Internet Explorer"==navigator.appName?(i.prototype.am=o,ar=30):fr&&"Netscape"!=navigator.appName?(i.prototype.am=n,ar=26):(i.prototype.am=s,ar=28),i.prototype.DB=ar,i.prototype.DM=(1<<ar)-1,i.prototype.DV=1<<ar;i.prototype.FV=Math.pow(2,52),i.prototype.F1=52-ar,i.prototype.F2=2*ar-52;var lr,cr,pr="0123456789abcdefghijklmnopqrstuvwxyz",vr=new Array;for(lr="0".charCodeAt(0),cr=0;cr<=9;++cr)vr[lr++]=cr;for(lr="a".charCodeAt(0),cr=10;cr<36;++cr)vr[lr++]=cr;for(lr="A".charCodeAt(0),cr=10;cr<36;++cr)vr[lr++]=cr;E.prototype.convert=A,E.prototype.revert=P,E.prototype.reduce=O,E.prototype.mulTo=R,E.prototype.sqrTo=M,H.prototype.convert=k,H.prototype.revert=V,H.prototype.reduce=N,H.prototype.mulTo=L,H.prototype.sqrTo=j,i.prototype.copyTo=a,i.prototype.fromInt=f,i.prototype.fromString=c,i.prototype.clamp=p,i.prototype.dlShiftTo=F,i.prototype.drShiftTo=b,i.prototype.lShiftTo=w,i.prototype.rShiftTo=B,i.prototype.subTo=x,i.prototype.multiplyTo=D,i.prototype.squareTo=I,i.prototype.divRemTo=q,i.prototype.invDigit=C,i.prototype.isEven=z,i.prototype.exp=K,i.prototype.toString=v,i.prototype.negate=y,i.prototype.abs=m,i.prototype.compareTo=d,i.prototype.bitLength=T,i.prototype.mod=S,i.prototype.modPowInt=Z,i.ZERO=l(0),i.ONE=l(1),Ct.prototype.convert=Ht,Ct.prototype.revert=Ht,Ct.prototype.mulTo=kt,Ct.prototype.sqrTo=Vt,zt.prototype.convert=Kt,zt.prototype.revert=Zt,zt.prototype.reduce=Ut,zt.prototype.mulTo=Xt,zt.prototype.sqrTo=_t;var yr=[2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97,101,103,107,109,113,127,131,137,139,149,151,157,163,167,173,179,181,191,193,197,199,211,223,227,229,233,239,241,251,257,263,269,271,277,281,283,293,307,311,313,317,331,337,347,349,353,359,367,373,379,383,389,397,401,409,419,421,431,433,439,443,449,457,461,463,467,479,487,491,499,503,509,521,523,541,547,557,563,569,571,577,587,593,599,601,607,613,617,619,631,641,643,647,653,659,661,673,677,683,691,701,709,719,727,733,739,743,751,757,761,769,773,787,797,809,811,821,823,827,829,839,853,857,859,863,877,881,883,887,907,911,919,929,937,941,947,953,967,971,977,983,991,997],mr=(1<<26)/yr[yr.length-1];i.prototype.chunkSize=G,i.prototype.toRadix=Q,i.prototype.fromRadix=W,i.prototype.fromNumber=$,i.prototype.bitwiseTo=nt,i.prototype.changeBit=bt,i.prototype.addTo=Dt,i.prototype.dMultiply=Rt,i.prototype.dAddOffset=Mt,i.prototype.multiplyLowerTo=jt,i.prototype.multiplyUpperTo=Lt,i.prototype.modInt=Jt,i.prototype.millerRabin=$t,i.prototype.clone=U,i.prototype.intValue=_,i.prototype.byteValue=X,i.prototype.shortValue=Y,i.prototype.signum=J,i.prototype.toByteArray=tt,i.prototype.equals=rt,i.prototype.min=it,i.prototype.max=et,i.prototype.and=st,i.prototype.or=ht,i.prototype.xor=ft,i.prototype.andNot=ct,i.prototype.not=pt,i.prototype.shiftLeft=vt,i.prototype.shiftRight=yt,i.prototype.getLowestSetBit=dt,i.prototype.bitCount=Tt,i.prototype.testBit=Ft,i.prototype.setBit=wt,i.prototype.clearBit=Bt,i.prototype.flipBit=xt,i.prototype.add=It,i.prototype.subtract=qt,i.prototype.multiply=St,i.prototype.divide=At,i.prototype.remainder=Pt,i.prototype.divideAndRemainder=Ot,i.prototype.modPow=Yt,i.prototype.modInverse=Qt,i.prototype.pow=Nt,i.prototype.gcd=Gt,i.prototype.isProbablePrime=Wt,i.prototype.square=Et,i.prototype.Barrett=zt;var dr,gr,Tr;if(null==gr){gr=new Array,Tr=0;var Fr;if("undefined"!=typeof window&&window.crypto)if(window.crypto.getRandomValues){var br=new Uint8Array(32);for(window.crypto.getRandomValues(br),Fr=0;Fr<32;++Fr)gr[Tr++]=br[Fr]}else if("Netscape"==navigator.appName&&navigator.appVersion<"5"){var wr=window.crypto.random(32);for(Fr=0;Fr<wr.length;++Fr)gr[Tr++]=255&wr.charCodeAt(Fr)}for(;Tr<Br;)Fr=Math.floor(65536*Math.random()),gr[Tr++]=Fr>>>8,gr[Tr++]=255&Fr;Tr=0,rr()}nr.prototype.nextBytes=er,or.prototype.init=sr,or.prototype.next=ur;var Br=256;r=t.exports={default:i,BigInteger:i,SecureRandom:nr}}).call(this)},function(t,r,i){"use strict";function e(t){if(Array.isArray(t)){for(var r=0,i=Array(t.length);r<t.length;r++)i[r]=t[r];return i}return Array.from(t)}function n(t,r){var i=31&r;return t<<i|t>>>32-i}function o(t,r){for(var i=[],e=t.length-1;e>=0;e--)i[e]=255&(t[e]^r[e]);return i}function s(t){return t^n(t,9)^n(t,17)}function u(t){return t^n(t,15)^n(t,23)}function h(t){var r=8*t.length,i=r%512;i=i>=448?512-i%448-1:448-i-1;for(var o=new Array((i-7)/8),h=new Array(8),a=0,c=o.length;a<c;a++)o[a]=0;for(var p=0,v=h.length;p<v;p++)h[p]=0;r=r.toString(2);for(var y=7;y>=0;y--)if(r.length>8){var m=r.length-8;h[y]=parseInt(r.substr(m),2),r=r.substr(0,m)}else r.length>0&&(h[y]=parseInt(r,2),r="");for(var d=new Uint8Array([].concat(e(t),[128],o,h)),g=new DataView(d.buffer,0),T=d.length/64,F=new Uint32Array([1937774191,1226093241,388252375,3666478592,2842636476,372324522,3817729613,2969243214]),b=0;b<T;b++){f.fill(0),l.fill(0);for(var w=16*b,B=0;B<16;B++)f[B]=g.getUint32(4*(w+B),!1);for(var x=16;x<68;x++)f[x]=u(f[x-16]^f[x-9]^n(f[x-3],15))^n(f[x-13],7)^f[x-6];for(var D=0;D<64;D++)l[D]=f[D]^f[D+4];for(var I=F[0],q=F[1],S=F[2],E=F[3],A=F[4],P=F[5],O=F[6],R=F[7],M=void 0,C=void 0,H=void 0,k=void 0,V=void 0,N=0;N<64;N++)V=N>=0&&N<=15?2043430169:2055708042,M=n(n(I,12)+A+n(V,N),7),C=M^n(I,12),H=(N>=0&&N<=15?I^q^S:I&q|I&S|q&S)+E+C+l[N],k=(N>=0&&N<=15?A^P^O:A&P|~A&O)+R+M+f[N],E=S,S=n(q,9),q=I,I=H,R=O,O=n(P,19),P=A,A=s(k);F[0]^=I,F[1]^=q,F[2]^=S,F[3]^=E,F[4]^=A,F[5]^=P,F[6]^=O,F[7]^=R}for(var j=[],L=0,z=F.length;L<z;L++){var K=F[L];j.push((4278190080&K)>>>24,(16711680&K)>>>16,(65280&K)>>>8,255&K)}return j}function a(t,r){for(r.length>c&&(r=h(r));r.length<c;)r.push(0);var i=o(r,p),n=o(r,v),s=h([].concat(e(i),e(t)));return h([].concat(e(n),e(s)))}for(var f=new Uint32Array(68),l=new Uint32Array(64),c=64,p=new Uint8Array(c),v=new Uint8Array(c),y=0;y<c;y++)p[y]=54,v[y]=92;t.exports={sm3:h,hmac:a}},function(t,r,i){"use strict";function e(t){if(Array.isArray(t)){for(var r=0,i=Array(t.length);r<t.length;r++)i[r]=t[r];return i}return Array.from(t)}function n(t,r){var i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:1;t="string"==typeof t?m.hexToArray(m.utf8ToHex(t)):Array.prototype.slice.call(t),r=m.getGlobalCurve().decodePointHex(r);var n=m.generateKeyPairHex(),o=new c(n.privateKey,16),s=n.publicKey;s.length>128&&(s=s.substr(s.length-128));var u=r.multiply(o),h=m.hexToArray(m.leftPad(u.getX().toBigInteger().toRadix(16),64)),a=m.hexToArray(m.leftPad(u.getY().toBigInteger().toRadix(16),64)),f=m.arrayToHex(d([].concat(h,t,a))),l=1,p=0,v=[],y=[].concat(h,a),g=function(){v=d([].concat(e(y),[l>>24&255,l>>16&255,l>>8&255,255&l])),l++,p=0};g();for(var T=0,F=t.length;T<F;T++)p===v.length&&g(),t[T]^=255&v[p++];var b=m.arrayToHex(t);return i===w?s+b+f:s+f+b}function o(t,r){var i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:1,n=arguments.length>3&&void 0!==arguments[3]?arguments[3]:{},o=n.output,s=void 0===o?"string":o;r=new c(r,16);var u=t.substr(128,64),h=t.substr(192);i===w&&(u=t.substr(t.length-64),h=t.substr(128,t.length-128-64));var a=m.hexToArray(h),f=m.getGlobalCurve().decodePointHex("04"+t.substr(0,128)),l=f.multiply(r),p=m.hexToArray(m.leftPad(l.getX().toBigInteger().toRadix(16),64)),v=m.hexToArray(m.leftPad(l.getY().toBigInteger().toRadix(16),64)),y=1,g=0,T=[],F=[].concat(p,v),b=function(){T=d([].concat(e(F),[y>>24&255,y>>16&255,y>>8&255,255&y])),y++,g=0};b();for(var B=0,x=a.length;B<x;B++)g===T.length&&b(),a[B]^=255&T[g++];return m.arrayToHex(d([].concat(p,a,v)))===u.toLowerCase()?"array"===s?a:m.arrayToUtf8(a):"array"===s?[]:""}function s(t,r){var i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},e=i.pointPool,n=i.der,o=i.hash,s=i.publicKey,u=i.userId,l="string"==typeof t?m.utf8ToHex(t):m.arrayToHex(t);o&&(s=s||a(r),l=h(l,s,u));var p=new c(r,16),y=new c(l,16),d=null,g=null,T=null;do{do{var F=void 0;F=e&&e.length?e.pop():f(),d=F.k,g=y.add(F.x1).mod(b)}while(g.equals(c.ZERO)||g.add(d).equals(b));T=p.add(c.ONE).modInverse(b).multiply(d.subtract(g.multiply(p))).mod(b)}while(T.equals(c.ZERO));return n?v(g,T):m.leftPad(g.toString(16),64)+m.leftPad(T.toString(16),64)}function u(t,r,i){var e=arguments.length>3&&void 0!==arguments[3]?arguments[3]:{},n=e.der,o=e.hash,s=e.userId,u="string"==typeof t?m.utf8ToHex(t):m.arrayToHex(t);o&&(u=h(u,i,s));var a=void 0,f=void 0;if(n){var l=y(r);a=l.r,f=l.s}else a=new c(r.substring(0,64),16),f=new c(r.substring(64),16);var p=F.decodePointHex(i),v=new c(u,16),d=a.add(f).mod(b);if(d.equals(c.ZERO))return!1;var g=T.multiply(f).add(p.multiply(d)),w=v.add(g.getX().toBigInteger()).mod(b);return a.equals(w)}function h(t,r){var i=arguments.length>2&&void 0!==arguments[2]?arguments[2]:"1234567812345678";i=m.utf8ToHex(i);var e=m.leftPad(T.curve.a.toBigInteger().toRadix(16),64),n=m.leftPad(T.curve.b.toBigInteger().toRadix(16),64),o=m.leftPad(T.getX().toBigInteger().toRadix(16),64),s=m.leftPad(T.getY().toBigInteger().toRadix(16),64),u=void 0,h=void 0;if(128===r.length)u=r.substr(0,64),h=r.substr(64,64);else{var a=T.curve.decodePointHex(r);u=m.leftPad(a.getX().toBigInteger().toRadix(16),64),h=m.leftPad(a.getY().toBigInteger().toRadix(16),64)}var f=m.hexToArray(i+e+n+o+s+u+h),l=4*i.length;f.unshift(255&l),f.unshift(l>>8&255);var c=d(f);return m.arrayToHex(d(c.concat(m.hexToArray(t))))}function a(t){var r=T.multiply(new c(t,16));return"04"+m.leftPad(r.getX().toBigInteger().toString(16),64)+m.leftPad(r.getY().toBigInteger().toString(16),64)}function f(){var t=m.generateKeyPairHex(),r=F.decodePointHex(t.publicKey);return t.k=new c(t.privateKey,16),t.x1=r.getX().toBigInteger(),t}var l=i(0),c=l.BigInteger,p=i(3),v=p.encodeDer,y=p.decodeDer,m=i(4),d=i(1).sm3,g=m.generateEcparam(),T=g.G,F=g.curve,b=g.n,w=0;t.exports={generateKeyPairHex:m.generateKeyPairHex,compressPublicKeyHex:m.compressPublicKeyHex,comparePublicKeyHex:m.comparePublicKeyHex,doEncrypt:n,doDecrypt:o,doSignature:s,doVerifySignature:u,getPublicKeyFromPrivateKey:a,getPoint:f,verifyPublicKey:m.verifyPublicKey}},function(t,r,i){"use strict";function e(t,r){if(!t)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return!r||"object"!=typeof r&&"function"!=typeof r?t:r}function n(t,r){if("function"!=typeof r&&null!==r)throw new TypeError("Super expression must either be null or a function, not "+typeof r);t.prototype=Object.create(r&&r.prototype,{constructor:{value:t,enumerable:!1,writable:!0,configurable:!0}}),r&&(Object.setPrototypeOf?Object.setPrototypeOf(t,r):t.__proto__=r)}function o(t,r){if(!(t instanceof r))throw new TypeError("Cannot call a class as a function")}function s(t){var r=t.toString(16);if("-"!==r[0])r.length%2==1?r="0"+r:r.match(/^[0-7]/)||(r="00"+r);else{r=r.substr(1);var i=r.length;i%2==1?i+=1:r.match(/^[0-7]/)||(i+=2);for(var e="",n=0;n<i;n++)e+="f";e=new c(e,16),r=e.xor(t).add(c.ONE),r=r.toString(16).replace(/^-/,"")}return r}function u(t,r){return+t[r+2]<8?1:128&+t.substr(r+2,2)}function h(t,r){var i=u(t,r),e=t.substr(r+2,2*i);return e?(+e[0]<8?new c(e,16):new c(e.substr(2),16)).intValue():-1}function a(t,r){return r+2*(u(t,r)+1)}var f=function(){function t(t,r){for(var i=0;i<r.length;i++){var e=r[i];e.enumerable=e.enumerable||!1,e.configurable=!0,"value"in e&&(e.writable=!0),Object.defineProperty(t,e.key,e)}}return function(r,i,e){return i&&t(r.prototype,i),e&&t(r,e),r}}(),l=i(0),c=l.BigInteger,p=function(){function t(){o(this,t),this.tlv=null,this.t="00",this.l="00",this.v=""}return f(t,[{key:"getEncodedHex",value:function(){return this.tlv||(this.v=this.getValue(),this.l=this.getLength(),this.tlv=this.t+this.l+this.v),this.tlv}},{key:"getLength",value:function(){var t=this.v.length/2,r=t.toString(16);return r.length%2==1&&(r="0"+r),t<128?r:(128+r.length/2).toString(16)+r}},{key:"getValue",value:function(){return""}}]),t}(),v=function(t){function r(t){o(this,r);var i=e(this,(r.__proto__||Object.getPrototypeOf(r)).call(this));return i.t="02",t&&(i.v=s(t)),i}return n(r,t),f(r,[{key:"getValue",value:function(){return this.v}}]),r}(p),y=function(t){function r(t){o(this,r);var i=e(this,(r.__proto__||Object.getPrototypeOf(r)).call(this));return i.t="30",i.asn1Array=t,i}return n(r,t),f(r,[{key:"getValue",value:function(){return this.v=this.asn1Array.map(function(t){return t.getEncodedHex()}).join(""),this.v}}]),r}(p);t.exports={encodeDer:function(t,r){var i=new v(t),e=new v(r);return new y([i,e]).getEncodedHex()},decodeDer:function(t){var r=a(t,0),i=a(t,r),e=h(t,r),n=t.substr(i,2*e),o=i+n.length,s=a(t,o),u=h(t,o),f=t.substr(s,2*u);return{r:new c(n,16),s:new c(f,16)}}}},function(t,r,i){"use strict";function e(){return b}function n(){var t=new y("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF",16),r=new y("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFC",16),i=new y("28E9FA9E9D9F5E344D5A9E4BCF6509A7F39789F515AB8F92DDBCBD414D940E93",16),e=new g(t,r,i);return{curve:e,G:e.decodePointHex("0432C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0"),n:new y("FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFF7203DF6B21C6052B53BBF40939D54123",16)}}function o(t,r,i){var e=t?new y(t,r,i):new y(B.bitLength(),T),n=e.mod(B.subtract(y.ONE)).add(y.ONE),o=h(n.toString(16),64),s=w.multiply(n);return{privateKey:o,publicKey:"04"+h(s.getX().toBigInteger().toString(16),64)+h(s.getY().toBigInteger().toString(16),64)}}function s(t){if(130!==t.length)throw new Error("Invalid public key to compress");var r=(t.length-2)/2,i=t.substr(2,r),e=new y(t.substr(r+2,r),16),n="03";return e.mod(new y("2")).equals(y.ZERO)&&(n="02"),n+i}function u(t){t=unescape(encodeURIComponent(t));for(var r=t.length,i=[],e=0;e<r;e++)i[e>>>2]|=(255&t.charCodeAt(e))<<24-e%4*8;for(var n=[],o=0;o<r;o++){var s=i[o>>>2]>>>24-o%4*8&255;n.push((s>>>4).toString(16)),n.push((15&s).toString(16))}return n.join("")}function h(t,r){return t.length>=r?t:new Array(r-t.length+1).join("0")+t}function a(t){return t.map(function(t){return t=t.toString(16),1===t.length?"0"+t:t}).join("")}function f(t){for(var r=[],i=0,e=0;e<2*t.length;e+=2)r[e>>>3]|=parseInt(t[i],10)<<24-e%8*4,i++;try{for(var n=[],o=0;o<t.length;o++){var s=r[o>>>2]>>>24-o%4*8&255;n.push(String.fromCharCode(s))}return decodeURIComponent(escape(n.join("")))}catch(t){throw new Error("Malformed UTF-8 data")}}function l(t){var r=[],i=t.length;i%2!=0&&(t=h(t,i+1)),i=t.length;for(var e=0;e<i;e+=2)r.push(parseInt(t.substr(e,2),16));return r}function c(t){var r=b.decodePointHex(t);if(!r)return!1;var i=r.getX();return r.getY().square().equals(i.multiply(i.square()).add(i.multiply(b.a)).add(b.b))}function p(t,r){var i=b.decodePointHex(t);if(!i)return!1;var e=b.decodePointHex(r);return!!e&&i.equals(e)}var v=i(0),y=v.BigInteger,m=v.SecureRandom,d=i(5),g=d.ECCurveFp,T=new m,F=n(),b=F.curve,w=F.G,B=F.n;t.exports={getGlobalCurve:e,generateEcparam:n,generateKeyPairHex:o,compressPublicKeyHex:s,utf8ToHex:u,leftPad:h,arrayToHex:a,arrayToUtf8:f,hexToArray:l,verifyPublicKey:c,comparePublicKeyHex:p}},function(t,r,i){"use strict";function e(t,r){if(!(t instanceof r))throw new TypeError("Cannot call a class as a function")}var n=function(){function t(t,r){for(var i=0;i<r.length;i++){var e=r[i];e.enumerable=e.enumerable||!1,e.configurable=!0,"value"in e&&(e.writable=!0),Object.defineProperty(t,e.key,e)}}return function(r,i,e){return i&&t(r.prototype,i),e&&t(r,e),r}}(),o=i(0),s=o.BigInteger,u=new s("2"),h=new s("3"),a=function(){function t(r,i){e(this,t),this.x=i,this.q=r}return n(t,[{key:"equals",value:function(t){return t===this||this.q.equals(t.q)&&this.x.equals(t.x)}},{key:"toBigInteger",value:function(){return this.x}},{key:"negate",value:function(){return new t(this.q,this.x.negate().mod(this.q))}},{key:"add",value:function(r){return new t(this.q,this.x.add(r.toBigInteger()).mod(this.q))}},{key:"subtract",value:function(r){return new t(this.q,this.x.subtract(r.toBigInteger()).mod(this.q))}},{key:"multiply",value:function(r){return new t(this.q,this.x.multiply(r.toBigInteger()).mod(this.q))}},{key:"divide",value:function(r){return new t(this.q,this.x.multiply(r.toBigInteger().modInverse(this.q)).mod(this.q))}},{key:"square",value:function(){return new t(this.q,this.x.square().mod(this.q))}}]),t}(),f=function(){function t(r,i,n,o){e(this,t),this.curve=r,this.x=i,this.y=n,this.z=null==o?s.ONE:o,this.zinv=null}return n(t,[{key:"getX",value:function(){return null===this.zinv&&(this.zinv=this.z.modInverse(this.curve.q)),this.curve.fromBigInteger(this.x.toBigInteger().multiply(this.zinv).mod(this.curve.q))}},{key:"getY",value:function(){return null===this.zinv&&(this.zinv=this.z.modInverse(this.curve.q)),this.curve.fromBigInteger(this.y.toBigInteger().multiply(this.zinv).mod(this.curve.q))}},{key:"equals",value:function(t){return t===this||(this.isInfinity()?t.isInfinity():t.isInfinity()?this.isInfinity():!!t.y.toBigInteger().multiply(this.z).subtract(this.y.toBigInteger().multiply(t.z)).mod(this.curve.q).equals(s.ZERO)&&t.x.toBigInteger().multiply(this.z).subtract(this.x.toBigInteger().multiply(t.z)).mod(this.curve.q).equals(s.ZERO))}},{key:"isInfinity",value:function(){return null===this.x&&null===this.y||this.z.equals(s.ZERO)&&!this.y.toBigInteger().equals(s.ZERO)}},{key:"negate",value:function(){return new t(this.curve,this.x,this.y.negate(),this.z)}},{key:"add",value:function(r){if(this.isInfinity())return r;if(r.isInfinity())return this;var i=this.x.toBigInteger(),e=this.y.toBigInteger(),n=this.z,o=r.x.toBigInteger(),u=r.y.toBigInteger(),h=r.z,a=this.curve.q,f=i.multiply(h).mod(a),l=o.multiply(n).mod(a),c=f.subtract(l),p=e.multiply(h).mod(a),v=u.multiply(n).mod(a),y=p.subtract(v);if(s.ZERO.equals(c))return s.ZERO.equals(y)?this.twice():this.curve.infinity;var m=f.add(l),d=n.multiply(h).mod(a),g=c.square().mod(a),T=c.multiply(g).mod(a),F=d.multiply(y.square()).subtract(m.multiply(g)).mod(a),b=c.multiply(F).mod(a),w=y.multiply(g.multiply(f).subtract(F)).subtract(p.multiply(T)).mod(a),B=T.multiply(d).mod(a);return new t(this.curve,this.curve.fromBigInteger(b),this.curve.fromBigInteger(w),B)}},{key:"twice",value:function(){if(this.isInfinity())return this;if(!this.y.toBigInteger().signum())return this.curve.infinity;var r=this.x.toBigInteger(),i=this.y.toBigInteger(),e=this.z,n=this.curve.q,o=this.curve.a.toBigInteger(),s=r.square().multiply(h).add(o.multiply(e.square())).mod(n),u=i.shiftLeft(1).multiply(e).mod(n),a=i.square().mod(n),f=a.multiply(r).multiply(e).mod(n),l=u.square().mod(n),c=s.square().subtract(f.shiftLeft(3)).mod(n),p=u.multiply(c).mod(n),v=s.multiply(f.shiftLeft(2).subtract(c)).subtract(l.shiftLeft(1).multiply(a)).mod(n),y=u.multiply(l).mod(n);return new t(this.curve,this.curve.fromBigInteger(p),this.curve.fromBigInteger(v),y)}},{key:"multiply",value:function(t){if(this.isInfinity())return this;if(!t.signum())return this.curve.infinity;for(var r=t.multiply(h),i=this.negate(),e=this,n=r.bitLength()-2;n>0;n--){e=e.twice();var o=r.testBit(n);o!==t.testBit(n)&&(e=e.add(o?this:i))}return e}}]),t}(),l=function(){function t(r,i,n){e(this,t),this.q=r,this.a=this.fromBigInteger(i),this.b=this.fromBigInteger(n),this.infinity=new f(this,null,null)}return n(t,[{key:"equals",value:function(t){return t===this||this.q.equals(t.q)&&this.a.equals(t.a)&&this.b.equals(t.b)}},{key:"fromBigInteger",value:function(t){return new a(this.q,t)}},{key:"decodePointHex",value:function(t){switch(parseInt(t.substr(0,2),16)){case 0:return this.infinity;case 2:case 3:var r=this.fromBigInteger(new s(t.substr(2),16)),i=this.fromBigInteger(r.multiply(r.square()).add(r.multiply(this.a)).add(this.b).toBigInteger().modPow(this.q.divide(new s("4")).add(s.ONE),this.q));return i.toBigInteger().mod(u).equals(new s(t.substr(0,2),16).subtract(u))||(i=i.negate()),new f(this,r,i);case 4:case 6:case 7:var e=(t.length-2)/2,n=t.substr(2,e),o=t.substr(e+2,e);return new f(this,this.fromBigInteger(new s(n,16)),this.fromBigInteger(new s(o,16)));default:return null}}}]),t}();t.exports={ECPointFp:f,ECCurveFp:l}}])});