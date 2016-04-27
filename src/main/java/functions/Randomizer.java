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

import java.util.Random;

/**
 * @author Yiqi (Eric) Hou
 *
 * <p>
 * Utilized XOR-Shift pseudo random algorithms to generate random numbers.
 * Can take objects as seeds, reading their byte representations and using those as seeds.
 */
public class Randomizer {
	public static long random(Object seed){
		long[] seeds = new long[2];
		if(seed == null){
			seeds[0] = randomLong();
			seeds[1] = randomLong();
		}else{
			seeds[0] = Serializer.extractLong(seed);
			seeds[1] = randomLong();
		}
		long x = seeds[0];
		final long y = seeds[1];
		seeds[0] = y;
		x ^= x << 23;
		seeds[1] = x ^ y ^ (x >> 17) ^ (y >> 26);
		return seeds[1] + y;
	}
	
	public static int randomInt(){
		Random r = new Random();
		int x = r.nextInt();
		int y = r.nextInt();
		int z = r.nextInt();
		int w = r.nextInt();
		int t = x;
		t ^= t << 11;
	    t ^= t >> 8;
	    x = y; y = z; z = w;
	    w ^= w >> 19;
	    w ^= t;
	    return w;
	}
	
	public static long randomLong(){
		Random r = new Random();
		long x = r.nextLong();
		long y = r.nextLong();
		long z = r.nextLong();
		long w = r.nextLong();
		long t = x;
		t ^= t << 11;
	    t ^= t >> 8;
	    x = y; y = z; z = w;
	    w ^= w >> 19;
	    w ^= t;
	    return w;
	}
}
