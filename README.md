# LocalBroadcastAnnotations
Android - Annotate methods to use as listeners for localbroadcast receivers.Inspired by [SensorAnnotations](https://github.com/dvoiss/SensorAnnotations).                     

I know lots of you use libraries like [EventBus](https://github.com/greenrobot/EventBus) to simplify code,but actually Android has a build-in component to achieve this,that is LocalBroadcastManager.

LocalBroadcast uses like Broadcast,but can only be registed in code,can't be registed in AndroidManifest.               
LocalBroadcast Usage is simple,            
Step1 init your receiver,like                   

````
BroadcastReceiver m_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	//add your code
        }
 }

````    
Step2 init your IntentFilter,like             

````
IntentFilter filter = new IntentFilter();
filter.addAction(action1);
filter.addAction(action2);
...
filter.addCategory(category1);
...
 ```` 
 Step3 register your LocalBroadcastReceiver.       
               
 ````
LocalBroadcastManager.getInstance(context).registerReceiver(receiver,filter);
 ```` 
 
### But,
When we have a lot of Actions and categories in filter,you must write a lot of dirty switch code in your receiver,like              

 ````
BroadcastReceiver m_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if(intent.getAction().equals(A)){
        		...
        	} else if (intent.getAction().equals(B)){
        		...
        	} else if (intent.getAction().equals(C)){
        		...
        	} else if (intent.getAction().equals(D)){
        		...
        	}
        }
 }
 ```` 
 pretty painful,right?                   
### Don't worry,now we have LocalBroadcastAnnotations

Usage is simple,                 

Step1 Annotate the method you want to act when receive those intent.       
              
 ````
@OnReceive("ACTION_TEXT_BLUE")
public void onTextBlue(Context context, Intent intent) {
    Toast.makeText(this, "onTextBlue", Toast.LENGTH_LONG).show();
}

@OnReceive("ACTION_TEXT_RED")
public void onTextRed(Context context, Intent intent) {
    Toast.makeText(this, "onTextRed", Toast.LENGTH_LONG).show();
}
 ```` 
 "ACTION_TEXT_BLUE" is the action string.
          
Step2 bind Annotation when Activity/Fragemnt resume          
                 
 ````
@Override
protected void onResume() {
    super.onResume();
    LocalBroadcastAnnotations.bind(this);
}
@Override
protected void onPause() {
    super.onPause();
	LocalBroadcastAnnotations.unbind(this);
}
 ```` 
Step3 sent your action!         
              
 ````
findViewById(R.id.tv_blue).setOnClickListener(new View.OnClickListener() {
	@Override
	public void onClick(View view) {
		Intent intent = new Intent();
		intent.setAction("ACTION_TEXT_BLUE");
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
});
 ```` 
 wola!
 
### TODO
* support class inherit.
 
 Enjoy yourself and check the code to know more details~




















 
