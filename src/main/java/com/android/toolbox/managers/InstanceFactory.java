package com.android.toolbox.managers;

/**
 * 
 */

 
import java.util.HashMap;  
import java.util.Map;  
  
public class InstanceFactory  
{  
	
	public interface ThingHolder<T>   
	{  
	    public T getThing();  
	    public void setThing(T toSet);  
	} 
  
    /***** 
     * The InstanceFactory is a Singleton to make sure there is only one instanceMap 
     *****/  
    private static final InstanceFactory factory = new InstanceFactory();  
    public static InstanceFactory getFactory() { return factory; }  
    private InstanceFactory() {};  
      
    /***** 
     * The Map holds Class -> ThingHolder pairings.  Note that the 'ThingHolder' represents the 'Singleton' 
     * you want per type. 
     *****/  
    @SuppressWarnings("unchecked")  
    private final Map<Class, ThingHolder> instanceMap = new HashMap<Class, ThingHolder>();  
  
    /***** 
     * Return an instance of a ThingHolder which holds an Object of the type described by 
     * the passed in Class.  Each type will have exactly one instance of ThingHolder, such 
     * that two calls to getInstance using the same Class Object will return the same  
     * instance of ThingHolder. 
     *  
     * Such that the assertion below should always be true: 
     * <code> 
     *     InstanceFactory factory = InstanceFactory.getFactory(); 
     *     ThingHolder<Integer> holder1 = factory.getInstance(Integer.class); 
     *     ThingHolder<Integer> holder2 = factory.getInstance(Integer.class); 
     *     assert(holder1 == holder2); 
     * </code> 
     *  
     * If different Class Objects are passed into the getInstance method then the method 
     * will return different ThingHolder objects which holds a different type. 
     *  
     * Such that the assertion below should always be true: 
     * <code> 
     *     InstanceFactory factory = InstanceFactory.getFactory(); 
     *     ThingHolder<Integer> holder1 = factory.getInstance(Integer.class); 
     *     ThingHolder<String> holder2 = factory.getInstance(String.class); 
     *     assert(holder1 != holder2); 
     * </code> 
     *****/  
    @SuppressWarnings("unchecked")  
    public <T> ThingHolder<T> getInstance(Class<T> ofClass) {  
        ThingHolder<T> instance = null;  
        synchronized(ofClass)  
        {  
            if (instanceMap.containsKey(ofClass))  
            {  
                instance = instanceMap.get(ofClass);  
            }  
            else  
            {  
                instance = new ThingHolderImpl<T>(ofClass);  
                instanceMap.put(ofClass, instance);  
            }  
        }  
          
        return instance;  
    }  
      
    /* *** 
     * Private implementation of the ThingHolder interface. 
     * ***/  
    private class ThingHolderImpl<T> implements ThingHolder<T>  
    {  
        private T toHold;  
          
        ThingHolderImpl(Class<T> typeToHold)  
        {
        	try {
				toHold = typeToHold.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
          
        public T getThing() { return this.toHold; }  
        public void setThing(T toSet) { this.toHold = toSet; }  
    }  
}  
 
