package com.andy.javalib.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;


public class JsonSchemaToBean {
	JSONObject schema = null;
	File outputDir = null;
	String packageName;
	int logIndent = -1;
	
	enum NodeType{
		object, array, string;
	}
	
	public JsonSchemaToBean fromSchema(InputStream schemaStream) throws JSONException, IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(schemaStream));
		String line = null;
		StringBuilder str = new StringBuilder();
		
		while((line = br.readLine()) != null){
			str.append(line);
		}
		
		schema = new JSONObject(str.toString());
		return this;
	}
	
	public void toFileInDir(String outputDir, String packageName) throws JSONException, IOException{
		debug("Java file will output to: "+outputDir);
		this.outputDir = new File(outputDir);
		this.packageName = packageName;
		resolveNode(schema);
		debug("All set! Please goto ["+outputDir +"] and check.");
	}
	
	private String resolveNode(JSONObject node) throws JSONException, IOException{
			return createJavaBean(node);
	}
	
	private String toClassName(String title){
		title = title.replaceAll("[^a-zA-Z]", "");
		title = title.substring(0,1).toUpperCase() + title.substring(1);
		return title;
	}
	
	private String createJavaBean(JSONObject node) throws IOException, JSONException {
		String className = toClassName(node.getString("title"));
		JSONObject beanNode = node.getJSONObject("properties");
		
		logIndent++;
		debug("Creating:"+className);
		
		File javaFile = new File(outputDir, className+".java");
		javaFile.createNewFile();
		
		try(BufferedWriter bw  = new BufferedWriter(new FileWriter(javaFile));)
		{
			bw.write("package ");
			bw.write(packageName);
			bw.write(";");
			bw.newLine();
			bw.write("/**");
			bw.newLine();
			bw.write(" * ");
			bw.write(node.optString("description",""));
			bw.newLine();
			bw.write(" */");
			bw.newLine();
			bw.write("public class ");
			bw.write(className);
			bw.write(" {");
			bw.newLine();
			
			@SuppressWarnings("rawtypes")
			Iterator keys = beanNode.keys();
			
			while(keys.hasNext()){
				String propName = (String)keys.next();
				JSONObject propNode = beanNode.getJSONObject(propName);
				
				if(propNode.keySet().isEmpty())
					continue;
				debug("- "+propName);
				NodeType propType = NodeType.valueOf(propNode.getString("type"));
				String propTypeStr = propType.toString();
				switch(propType ){
				case array: 
					propTypeStr = resolveNode(propNode.getJSONObject("items"));
					propTypeStr += "[]";
					break;
				case object: 
					propTypeStr = resolveNode(propNode.getJSONObject("properties"));
					break;
				case string: 
					propTypeStr = "String";
					break;
				}
				
				//output the java class attributes			
				bw.write( genPropDeclare(propTypeStr, propName, propNode.optString("description")));
				bw.newLine();
				//output the getter/setter
				bw.write( genGetMethod(propTypeStr, propName));
				bw.newLine();
				bw.write( genSetMethod(propTypeStr, propName));
				bw.newLine();
			}
			
			bw.newLine();
			bw.write("}");
		}
		logIndent --;
		return className;
	}

	private String genPropDeclare(String type, String propName, String comment){
		final String fmt = "/**\n * %3$s\n */\nprivate %1$s %2$s;";
		return String.format(fmt, type, propName, comment);
	}
	
	private String genGetMethod(String type, String propName) {
		final String fmt = "public %1$s get%2$s(){return this.%3$s;}";
		return String.format(fmt, type, upperFirst(propName), propName);
	}

	private String genSetMethod(String type, String propName) {
		final String fmt = "public void set%1$s(%2$s %3$s){this.%3$s = %3$s;}";
		return String.format(fmt, upperFirst(propName), type, propName);
	}
	
	private Object upperFirst(String propName) {
		return propName.substring(0,1).toUpperCase()+propName.substring(1);
	}

	private  void debug(String msg){
		for(int i = 0; i <= logIndent; i++){
			System.out.print("  ");
		}
		System.out.println(msg);
	}
	
	public static void main(String[] args) throws IOException, JSONException{
		try(InputStream in = JsonSchemaToBean.class.getResourceAsStream("schema.json");){
			new JsonSchemaToBean()
			.fromSchema(in)
			.toFileInDir("C:\\Java\\gen", "com.ibm.scw.commerce.rest.resource.personaldata");
		} 
	}
	
}
