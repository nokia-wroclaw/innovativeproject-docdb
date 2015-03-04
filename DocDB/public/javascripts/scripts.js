
			function addAutoComp(newEntry, name){
				var temp = localStorage[name];
				var array = []
				if(temp != null)
					array = temp.split(",");
				array[array.length] = newEntry;
				array = removeDup(array);
				localStorage[name] = array.toString();
			}
			function removeDup(array){
				//O(n^2)
				var uniqueNames = [];
				$.each(array, function(i, el){
					if($.inArray(el, uniqueNames) === -1) uniqueNames.push(el);
				});
				return uniqueNames;

			}
			function changeTag(tagIDName){
				var newTag = document.getElementById("text"+tagIDName).value;
				var	tagID = document.getElementById("text"+tagIDName);
				newTag = newTag.replace(/[^a-zA-Z0-9]*/g,"");
				tagID.value = newTag;
				localStorage[tagIDName] = newTag;
				tag[tagIDName] = newTag+","+$('#geoLoc').html();
				$(".dz-message span").eq(tagIDName).text('#'+newTag);
				addAutoComp(newTag, 'oldTags');
				document.getElementById("text"+tagIDName).value = "";
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
			
			function getTags(index){
				var temp = $('#geoLoc').html();				
				temp = temp.replace("# ","").replace("#","").replace(" ,", ",").replace(", ", ",");
				var arr = temp.split(", ");
				arr = removeDup(arr);

				return arr;
			}

			function isPic(fileType){
				type = fileType.split("/");
				if(type[0] == "image")
					return true;
				else
					return false;
			}
			
			function changeDropzones(number, color0, color1, color2, checked){
				//changing color part
				localStorage["numberOfDropzones"] = number;
				temp = [color0, color1, color2];
				for(i = 0; i < 3; i++) {
					if(!temp[i] == ''){
						localStorage["color"+i] = temp[i];
					}
				}
				//inclusion part
				if(checked == true){
					localStorage["inclusionCheck"] = true;
				} else {
					localStorage["inclusionCheck"] = false;
				}
				location.reload(); // we need to reload, to apply changes
			}
			
			function getRandomColor() {
				var letters = '0123456789ABCDEF'.split('');
				var color = '#';
				for (var i = 0; i < 6; i++ )
					color += letters[Math.floor(Math.random() * 16)];
				
				return color;
			}
			
			function getFileType(fileType){
				type = fileType.split("/");
				if(fileType == 'undefined') //zips
					return ".png";
				availableTypes = ['pdf', "msword", "plain", "" ,"audio", "video"]
				for(i = 0; i < availableTypes.length; i++){
					if(type[0] == availableTypes[i] || type[1] == availableTypes[i]){
						return availableTypes[i]+".png"
					}
				}
				return "file.png"
			}