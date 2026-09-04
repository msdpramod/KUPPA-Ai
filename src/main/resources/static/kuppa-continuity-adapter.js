(()=>{
  const stage=document.getElementById('stage');
  const resolve=name=>typeof globalThis[name]==='function'?globalThis[name].bind(globalThis):null;
  const api=Object.freeze({
    version:'v1',
    isAvailable(){return !!(resolve('issueOwnerContinuity')&&resolve('restoreContinuity')&&resolve('forgetOwnerDevice'))},
    async activateTrustedContinuity(){const issue=resolve('issueOwnerContinuity');return issue?!!(await issue()):false},
    async restoreContinuity(){const restore=resolve('restoreContinuity');if(!restore)return false;await restore();return true},
    forgetDevice(){const forget=resolve('forgetOwnerDevice');if(!forget)return false;forget();return true}
  });
  Object.defineProperty(globalThis,'KuppaContinuityAdapter',{value:api,writable:false,configurable:false,enumerable:false});
  stage?.dispatchEvent(new CustomEvent('kuppa-continuity-adapter-ready',{detail:{version:api.version,available:api.isAvailable()}}));
})();
