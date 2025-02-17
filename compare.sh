red='\033[0;31m'
green='\033[0;32m'
yellow='\033[0;33m'
blue='\033[0;34m'
purple='\033[0;35m'
cyan='\033[0;36m'
white='\033[0;37m'
end='\033[0m'

help(){
    printf "Run：$red sh $0 $green<verb> $yellow<args>$end\n"
    format="  $green%-6s $yellow%-8s$end%-20s\n"
    printf "$format" "-h" "" "帮助"
}

server='localhost:3300'

compare(){
    no=$1
    echo ">>>>>>> no: $no"
    has=$(curl -s "$server/tg-easy-fetch/task/compare/$no/false" | grep '该记录对应申请未审核通过' | wc -l)
    echo $has 
    if [ $has -gt 0 ]; then 
        echo '>>>>>>> start audit'
        curl "$server/tg-easy-fetch/task/autoAudit?no=$no"
    fi

    echo '>>>>>>> 比较'
    curl -s "$server/tg-easy-fetch/task/compare/$no/false" | pretty-json
    notify-send -i /home/zk/Pictures/icon/支付成功.svg 核对完成
}


compareBrand(){
    no=$1
    echo ">>>>>>> no: $no"
    has=$(curl -s "$server/tg-easy-fetch/task/compareB/$no/false" | grep '该记录对应申请未审核通过' | wc -l)
    echo $has 
    if [ $has -gt 0 ]; then 
        echo '>>>>>>> start audit'
        curl -s "$server/tg-easy-fetch/task/autoAudit?brand=true&no=$no"
        return 0
    fi

    echo '>>>>>>> 比较'
    curl -s "$server/tg-easy-fetch/task/compareB/$no/false" | pretty-json
    notify-send -i /home/zk/Pictures/icon/支付成功.svg 核对完成
}

audit_all_brand(){
    # curl 'localhost:8082/tg-easy-fetch/table_management/application/autoAudit?batch=262' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/118.0' -H 'Accept: application/json, text/plain, */*' -H 'Accept-Language: en-US,en;q=0.5' -H 'Accept-Encoding: gzip, deflate' -H "Authorization: $token" -H 'Connection: keep-alive' -H 'Referer: http://tgysg-test.sinohealth.cn/HomePage/UserHomePage' -H 'Cookie: JSESSIONID=5743D65356C827989EC669A46134A16D; Admin-Token=eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbl91c2VyX2tleSI6IjgzM2U1NTdlLTU2OTMtNDczMy04OWE0LTNjNTJlYThkMjdkMCJ9.iCuQ-atSWsYS4jrBTfqcRTul7PutbfEJJ_5bk4kfcfE; sidebarStatus=1; Admin-Token-TG=eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbl91c2VyX2tleSI6IjBjZjgxYjRlLWNhMjQtNDNmNS1iNjI3LTMwMjdkNGQyNDg3MyJ9.WYRGkmMMwyOrXa5-SDtbdbz_1vSd3B9p9Ac-5sgB8t8' -H 'Pragma: no-cache' -H 'Cache-Control: no-cache'
    curl "$server/tg-easy-fetch/task/autoAudit?brand=true&batch=500"
}

audit_all(){
    # curl 'localhost:8082/tg-easy-fetch/table_management/application/autoAudit?batch=262' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/118.0' -H 'Accept: application/json, text/plain, */*' -H 'Accept-Language: en-US,en;q=0.5' -H 'Accept-Encoding: gzip, deflate' -H "Authorization: $token" -H 'Connection: keep-alive' -H 'Referer: http://tgysg-test.sinohealth.cn/HomePage/UserHomePage' -H 'Cookie: JSESSIONID=5743D65356C827989EC669A46134A16D; Admin-Token=eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbl91c2VyX2tleSI6IjgzM2U1NTdlLTU2OTMtNDczMy04OWE0LTNjNTJlYThkMjdkMCJ9.iCuQ-atSWsYS4jrBTfqcRTul7PutbfEJJ_5bk4kfcfE; sidebarStatus=1; Admin-Token-TG=eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbl91c2VyX2tleSI6IjBjZjgxYjRlLWNhMjQtNDNmNS1iNjI3LTMwMjdkNGQyNDg3MyJ9.WYRGkmMMwyOrXa5-SDtbdbz_1vSd3B9p9Ac-5sgB8t8' -H 'Pragma: no-cache' -H 'Cache-Control: no-cache'
    curl "$server/tg-easy-fetch/task/autoAudit?batch=500"
}

case $1 in 
    -h)
        help ;;
    -all)
        audit_all ;;
    -ab)
        audit_all_brand ;;
    -b) 
        # id为-1时比对全部
        compareBrand $2;; 
    *)
        compare $1;;
esac