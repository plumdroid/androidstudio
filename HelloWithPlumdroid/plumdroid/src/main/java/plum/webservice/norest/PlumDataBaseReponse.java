package plum.webservice.norest;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;


public final class PlumDataBaseReponse {

		public final int etat;
		public final String message;
		public final String controleur;
		public final String action;
		public final String secure_token;
		
		public final PDO pdo;

		public PlumDataBaseReponse(String response) throws PlumDataBaseException {

            JSONObject jsonReponse = null;
            try{
                jsonReponse= new JSONObject(response);
            }catch(JSONException e){
                throw new PlumDataBaseException("Error create JSONObject "+e.toString(), "" ,"");
            }

            // Récupération des données

			try{
					etat=jsonReponse.getInt("etat");
					message=jsonReponse.getString("message");
					controleur=jsonReponse.getString("controleur");
					action=jsonReponse.getString("action");
					secure_token=jsonReponse.getString("secure_token");
					
					if(jsonReponse.has("pdo")){
						pdo=new PDO(jsonReponse.getJSONObject("pdo"));
					}
					else{
						pdo=null;
					}
				
			}catch(JSONException e){
				throw new PlumDataBaseException("Error JSON "+e.toString(),"","");
			}	
		}


	    public class PDO {
	    	
	    	public final int error;//0 tout est ok
	    	
	    	public final String errorCode;
			public final String errorInfo;
			public final long rowCount;
			public final String lastInsertId;
			
			public final ArrayList<ContentValues> listContentValues;
			
	    	public PDO(JSONObject jsonPdo)throws PlumDataBaseException{
	    		try{
	    			
	    			rowCount=jsonPdo.getLong("rowCount");

					if (jsonPdo.has("lastInsertId")) {
						lastInsertId = jsonPdo.getString("lastInsertId");
					}else{
						lastInsertId = "";
					}
	    			
	    			JSONObject jsonError=jsonPdo.getJSONObject("error");
	    			errorCode=jsonError.getString("errorCode");
	    			errorInfo=jsonError.getString("errorInfo");



	    			if(errorCode.equals("0")){error=0;}
	    			else{error=-1;}
	    			
	    			if(jsonPdo.has("tupple")){
	    				//cursor=buildCursor(jsonPdo.getJSONArray("tupple"));
						listContentValues = buildListContentValues(jsonPdo.getJSONArray("tupple"));
	    			}
	    			else{
						listContentValues = new ArrayList();
	    			}
				
			}catch(JSONException e){
				throw new PlumDataBaseException("Error JSON "+e.toString(),"","");
			}
	    	}

	    	/*
	    	* Retourne sous la forme ContentValues un ArrayList de la requête réalisée
	    	* Un ligne tupple = un ContentValue contenant des couples  couple (nomColonne,valeur)
	    	*
	    	*
	    	*/
			private ArrayList<ContentValues> buildListContentValues(JSONArray jsonTupple) throws PlumDataBaseException{
				if(rowCount == 0){ return new ArrayList<ContentValues>() ; }

                ArrayList<ContentValues> listContentValues = new ArrayList<ContentValues>();
				try{

				    //column
					JSONObject jsonLine = jsonTupple.getJSONObject(0);
					Iterator<String> iteratorKeys = jsonLine.keys();
					ArrayList<String> arrayKeys = new ArrayList<String>();

					int i=0;
					while(iteratorKeys.hasNext()){
						arrayKeys.add(iteratorKeys.next());
					}

					String[] column = new String[arrayKeys.size()];
					column = arrayKeys.toArray(column);


					for(i = 0; i < jsonTupple.length(); i++){

						jsonLine = jsonTupple.getJSONObject(i);

						ContentValues row = new ContentValues();
						for(int j=0; j<column.length; j++){
							row.put(column[j],jsonLine.getString(column[j]));
						}

						listContentValues.add(row);
					}

				}catch(JSONException e){
					throw new PlumDataBaseException("Error PDO.buildListContentValues "+e.toString(),"","");
				}

				return  listContentValues;
			}

			// buildCursor est ... !!!! OBSOLETE ... !!!!
	    	private Cursor buildCursor(JSONArray jsonTupple) throws PlumDataBaseException{
		    	
		    	if(rowCount==0){return new MatrixCursor(new String[]{"_id"});}
		    	
		    	MatrixCursor cursor=null;

		    	
		    	String[] column;
		    	String[] row;
		    	
		    	try{
		    		
		    		
		    		//column
		    		JSONObject jsonLine=jsonTupple.getJSONObject(0);
		    		Iterator<String> iteratorKeys=jsonLine.keys();
		    		ArrayList<String> arrayKeys=new ArrayList<String>();
		    		
		    		int i=0;
		    		while(iteratorKeys.hasNext()){
		    			arrayKeys.add(iteratorKeys.next());
		    			i++;
		    		}
		    		
		    		column=new String[arrayKeys.size()];
		    		column=arrayKeys.toArray(column);
		    		
		    		cursor=new MatrixCursor(column);
		    		
		    		//row
		    		row=new String[column.length];
		    		
		    		for(i=0;i<jsonTupple.length();i++){		
		    			jsonLine=jsonTupple.getJSONObject(i);
		    			
		    			for(int j=0;j<column.length;j++){
		    				row[j]=jsonLine.getString(column[j]);
		    			}
		    			
		    			cursor.addRow(row);
		    		}
		    		
		    	}catch(JSONException e){
		    		throw new PlumDataBaseException("Error PDO.buildCursor "+e.toString(),"","");
		    	}
				/*try {
					  BufferedReader reader =  new BufferedReader(new InputStreamReader(response,
					  "iso-8859-1"),8);
					  line = reader.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("getDataFormat",e.toString());
				}
				if(line==null) {return new MatrixCursor(new String[]{"_id"});}
				char[] charline=line.toCharArray();
				String[] column=getColumnFromWebService(charline,THE_NAME);
				cursor=new MatrixCursor(column);
				
				String[] row=getColumnFromWebService(charline,THE_VALUE);
				while (row!=null)
				{	cursor.addRow(row);row=getColumnFromWebService(charline,THE_VALUE);}*/
				return  cursor;
		    	
		    	
		    }
		}

	    
	}