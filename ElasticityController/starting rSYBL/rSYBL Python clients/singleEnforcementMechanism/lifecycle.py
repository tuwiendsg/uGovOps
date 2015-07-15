import urllib, urllib2, sys, httplib

url = "/rSYBL/restWS"
#HOST_IP="83.212.112.35"
#HOST_IP="128.130.172.214:8080"
#HOST_IP="83.212.112.148"
#HOST_IP="128.130.172.214:8280"
#HOST_IP="109.231.121.26:8280"
#HOST_IP="109.231.122.193:8081"
HOST_IP="localhost:8080"
CLOUD_APPLICATION_ID="IoTDaaSControl"

if __name__=='__main__':
	connection =  httplib.HTTPConnection(HOST_IP)
    #read composition rules file
	headers={'Content-Type':'application/xml; charset=utf-8','Accept':'application/xml, multipart/related'}
	#############Prepare for rSYBL control, new application to be controlled is being described
	connection.request('PUT', url+'/'+CLOUD_APPLICATION_ID+'/prepareControl', body=CLOUD_APPLICATION_ID,headers=headers,)
	result = connection.getresponse()
	print result.read()
	########################Current DeploymentDescription
	connection =  httplib.HTTPConnection(HOST_IP)
    #read composition rules file
	composition_file = open("./deploymentDescription_.xml", "r")
	body_content =  composition_file.read()
	connection.request('PUT', url+'/'+CLOUD_APPLICATION_ID+'/deployment', body=body_content,headers=headers,)
	result = connection.getresponse()
	print result.read()
	#######################Current Application Description - e.g., requirements, structural stuff..
	connection =  httplib.HTTPConnection(HOST_IP)
    #read service description file
	composition_file = open("./serviceDescription_.xml", "r")
	body_content =  composition_file.read()
	connection.request('PUT', url+'/'+CLOUD_APPLICATION_ID+'/description', body=body_content,headers=headers,)
	result = connection.getresponse()
	print result.read()

        #read composition rules file
	composition_file = open("./compositionRules.xml", "r")
	body_content =  composition_file.read()
	connection.request('PUT', url+'/'+CLOUD_APPLICATION_ID+'/compositionRules', body=body_content,headers=headers,)
	result = connection.getresponse()
	print result.read()

        headers={'Content-Type':'application/json; charset=utf-8','Accept':'application/json, multipart/related'}
       #read elasticity capabilities effects file
	composition_file = open("./effects.json", "r")
	body_content =  composition_file.read()
	connection.request('PUT', url+'/'+CLOUD_APPLICATION_ID+'/elasticityCapabilitiesEffects', body=body_content,headers=headers,)
	result = connection.getresponse()
	print result.read()
        
        headers={'Content-Type':'application/xml; charset=utf-8','Accept':'application/xml, multipart/related'}
        connection.request('PUT', url+'/'+CLOUD_APPLICATION_ID+'/startControl', body=CLOUD_APPLICATION_ID,headers=headers,)
	result = connection.getresponse()
	print result.read()
      
 
  

 

