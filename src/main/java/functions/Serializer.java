/*   
  	This file is part of JSON Web Framework.

    JSON Web Framework is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JSON Web Framework is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with JSON Web Framework.  If not, see <http://www.gnu.org/licenses/>.
*/

package functions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * @author Yiqi (Eric) Hou
 *
 * <p>
 * A class that serializes/deserializes objects and their byte representations.
 */
public class Serializer {
	public static byte[] serialize(Object obj) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    return out.toByteArray();
	}
	public static <T> T deserialize(byte[] data) throws IOException, ClassNotFoundException{
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    return (T) is.readObject();
	}
	public static long extractLong(byte[] data) throws IOException, ClassNotFoundException{
		ByteArrayInputStream in = new ByteArrayInputStream(Arrays.copyOfRange(data, 0, 8));
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readLong();
	}
	/**
	 * Attempts to extract a long out of the serialized byte representation of an object.
	 * 
	 * @param obj	Object seed
	 * @return		The first long available from its serialized byte representation.
	 */
	public static long extractLong(Object obj){
		try{
			return extractLong(serialize(obj));
		}catch(Exception e){
			return Randomizer.randomLong();
		}
	}
}
