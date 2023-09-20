import Cookies from 'js-cookie'

const tokenKey: string = 'Authorization'

function setToken(token:string) {
    Cookies.set(tokenKey, token,{expires: 3})
}

function getToken(name: string = tokenKey) {
    return Cookies.get(name)
}

function removeToken(name: string = tokenKey) {
    Cookies.remove(name)
}

export  {setToken,getToken,removeToken}
