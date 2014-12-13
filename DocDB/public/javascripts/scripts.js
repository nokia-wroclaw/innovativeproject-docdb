					
			function addAutoComp(newEntry, name){
				var names = ["Mike","Matt","Nancy","Adam","Jenny","Nancy","Carl"];
				
				var temp = localStorage[name];
				var array = []
				if(temp != null)
					array = temp.split(",");
				array[array.length] = newEntry;
				//O(n^2) 
				var uniqueNames = [];
				$.each(array, function(i, el){
					if($.inArray(el, uniqueNames) === -1) uniqueNames.push(el);
				});
				localStorage[name] = uniqueNames.toString();
			}	
			
			function changeTag(tagIDName){
				var tag = document.getElementById(tagIDName).value;
				var	tagID = document.getElementById(tagIDName);
				tagID.value = tag;
				localStorage[tagIDName] = tag;
				addAutoComp(tag, 'oldTags');
				//location.reload();
			}

			function checkKey(tagID){
				if (event.keyCode == 13) {
					if (event.keyCode == 13) {
						if(tagID == 'search'){
							addAutoComp(document.getElementById(tagID).value, 'oldSearch');
							return false;
						}
						if(tagID == 'link'){
							handleLink(tagID);
							return false;
						} else {
							changeTag(tagID);
							return false;
						}
					}
				
				}else
					return false;
			}

			function handleLink(tagIDName){
				var link = document.getElementById(tagIDName).value;
				sendLink(link);
				return false;
			}		
		
			