package at.ac.tuwien.infosys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import at.ac.tuwien.infosys.Main.Decison.DECISION;
import at.ac.tuwien.infosys.model.uncertain.Device;
import at.ac.tuwien.infosys.model.uncertain.*;

public class Main {

	// Limitations:
	// - works only with discrete values
	// - each key of meta must be set (value can be null) for all devices

	// User input:
	// 1. decision threshold (default=0.5);
	// 2. replacement (e.g., "location=?&type=+&quality=-")
	// 3. Selection query
	// 4. Otimistic/pesimistic (maybe reduct in the future)

	BiFunction<? super List<Device>, ? super List<Device>, ? extends List<Device>> bi = (
			l, d) -> {
		l.addAll(d);
		return l;
	};

	public static void main(String[] args) {
		//+ will produce false positives and no false negatives thus it is optimistic
		
		//? will produce false negatives and no false positives thus it is pessimistic
		new Main().start(0.6,
//				"location=x&owner=stefanORowner=stefan&type=one",
				"location=location1&owner=stefan",
				"location=?&owner=+&type=+");
	}

	public void startPaperData() {
		Device d1 = new Device("D1");
		d1.addMeta("capacity", "two");
		d1.addMeta("noise", "-");
		d1.addMeta("size", "compact");
		// d1.setDecision(new Decison(d1).getDecision());
		d1.setDecision(DECISION.GOOD);

		Device d2 = new Device("D2");
		d2.addMeta("capacity", "four");
		d2.addMeta("noise", "*");
		d2.addMeta("size", "*");
		// d2.setDecision(new Decison(d2).getDecision());
		d2.setDecision(DECISION.GOOD);

		Device d3 = new Device("D3");
		d3.addMeta("capacity", "?");
		d3.addMeta("noise", "medium");
		d3.addMeta("size", "medium");
		// d3.setDecision(new Decison(d3).getDecision());
		d3.setDecision(DECISION.BAD);

		Device d4 = new Device("D4");
		d4.addMeta("capacity", "+");
		d4.addMeta("noise", "low");
		d4.addMeta("size", "compact");
		d4.setDecision(DECISION.BAD);

		Device d5 = new Device("D5");
		d5.addMeta("capacity", "four");
		d5.addMeta("noise", "?");
		d5.addMeta("size", "medium");
		d5.setDecision(DECISION.GOOD);

		Device d6 = new Device("D6");
		d6.addMeta("capacity", "-");
		d6.addMeta("noise", "medium");
		d6.addMeta("size", "full");
		d6.setDecision(DECISION.BAD);

		Device d7 = new Device("D7");
		d7.addMeta("capacity", "five");
		d7.addMeta("noise", "low");
		d7.addMeta("size", "full");
		d7.setDecision(DECISION.GOOD);

		Device d8 = new Device("D8");
		d8.addMeta("capacity", "five");
		d8.addMeta("noise", "low");
		d8.addMeta("size", "full");
		d8.setDecision(DECISION.GOOD);
		
		Device d9 = new Device("D9");
		d9.addMeta("capacity", "five");
		d9.addMeta("noise", "low");
		d9.addMeta("size", "full");
		d9.setDecision(DECISION.GOOD);
		
		Device d10 = new Device("D10");
		d10.addMeta("capacity", "five");
		d10.addMeta("noise", "low");
		d10.addMeta("size", "full");
		d10.setDecision(DECISION.GOOD);
		
		List<Device> devices = new ArrayList<Device>();
		devices.add(d1);
		devices.add(d2);
		devices.add(d3);
		devices.add(d4);
		devices.add(d5);
		devices.add(d6);
		devices.add(d7);
		devices.add(d8);
		devices.add(d9);
		devices.add(d10);
		
		List<Device> devicesNoMissing = prepareData(devices,
				"location=?&type=+&quality=-");
		printDataSetAsTable(devicesNoMissing);

		Map<Block, List<Device>> blocks = makeBlocks(devicesNoMissing);
		// printBlocks(blocks);
		Map<Block, List<Device>> processedBblocks = handleSpecialValues(blocks,
				devicesNoMissing);
		System.out.println();
		printBlocks(processedBblocks);

		System.out.println("\n");
		// System.out.println("Ka SET:");
		List<String> attributes = new ArrayList<String>();
		// attributes.add("size");
		// attributes.add("noise");
		// attributes.add("capacity");
		// System.out.println(buildCaracteristicSet(d7, attributes,
		// processedBblocks, devices));

		List<Device> original = new ArrayList<>();
		original.add(d1);
		original.add(d2);
		original.add(d5);
		original.add(d7);

		System.out.println(">>>>>>>>>>> Original Set <<<<<<<<<<<<<<");
		System.out.println(original);
		System.out.println(">>>>>>>>>>> B lower approximation <<<<<<<<<<<<<<");
		System.out.println(buildLowerApproximation(original, devices,
				attributes, processedBblocks));
		System.out.println(">>>>>>>>>>> B upper approximation <<<<<<<<<<<<<<");
		System.out.println(buildUpperApproximation(original, devices,
				attributes, processedBblocks));

	}

	public void start(double threshhold, String query, String replacement) {
		
		List<Device> devices = TrainingSet10.devices;

		System.out.println(query);
		List<Device> devicesNoMissing = prepareData(devices, replacement);
		printDataSetAsTable(devicesNoMissing);

		Map<Block, List<Device>> blocks = makeBlocks(devicesNoMissing);
		// printBlocks(blocks);
		Map<Block, List<Device>> processedBblocks = handleSpecialValues(blocks,
				devicesNoMissing);
		System.out.println();
		printBlocks(processedBblocks);

		System.out.println("\n");

		List<String> attributes = new ArrayList<String>();
		attributes.add("owner");
		attributes.add("location");
		attributes.add("type");

		List<Device> original = getDevicesForORQuery(devicesNoMissing, query);
		// new ArrayList<>();
		// original.add(d1);
		// original.add(d2);
		// original.add(d5);
		// original.add(d7);

//		System.out.println(">>>>>>>>>>> Original Set <<<<<<<<<<<<<<");
//		System.out.println(original);
		System.out.println(">>>>>>>>>>> B lower approximation <<<<<<<<<<<<<<");
		Set<Device> roughL = buildLowerApproximationOptimized(original, devices,
				attributes, processedBblocks);
//		System.out.println();
//		System.out.println(">>>>>>>>>>> B upper approximation <<<<<<<<<<<<<<");
//		System.out.println(buildUpperApproximation(original, devices,
//				attributes, processedBblocks));
		System.out.println(">>>>>>>>>>> RESULTS <<<<<<<<<<<<<<");
		
		List<Device> actual =  getActual(devicesNoMissing);
		System.out.println("Actual  >> " + actual);
		Set<Device> roughU = buildUpperApproximation(original, devices,attributes, processedBblocks);
		System.out.println("RoughU  >> " + roughU);
		System.out.println("Dumb    >> " + original);
		
		int actualSize = getActual(devicesNoMissing).size();
		System.out.println("Actual size  >> " + actualSize);
		int roughSize = roughU.size();
		System.out.println("RoughU size  >> " + roughSize);
		int dumbSize =  original.size();
		System.out.println("Dumb size  >> " +dumbSize);
		System.out.println();
		System.out.println("% of missing data = " + percentMissing(devicesNoMissing));
//		if (roughSize > actualSize){
//			//false positives
//			System.out.println("FP roughU = "+ (double)(roughSize-actualSize)/actualSize);
//		}else{
//			//false negatives or perfect match
//			System.out.println("FN roughU = "+ ((double)(roughSize-actualSize)/actualSize));
//			System.out.println("FN dumb= "+ (double)(dumbSize-actualSize)/actualSize);
//				
//			
//		}
//		System.out.println();
//		Set<Device> relevant = new HashSet<>(actual);
//		Set<Device> selected = new HashSet<>(roughU);
//		//calculate fn
//		relevant.removeAll(selected);
//		System.out.println("FN elements >> "+relevant);
//		
//		Set<Device> relevant1 = new HashSet<>(actual);
//		Set<Device> selected1 = new HashSet<>(roughU);
//		//calculate fp
//		selected1.removeAll(relevant1);
//		System.out.println("FP elements >> "+selected1);
//		
//		double p = (double)(actualSize-relevant.size())/roughSize;
//		double r = (double)(actualSize-relevant.size())/actualSize;
//		double fscore= 2*p*r/(p+r);
//		System.out.println("RoughU F score = "+ fscore);
//		// % of the identified resources which are false negatives
//		System.out.println("FN rate(%)= "+ (double) relevant.size()/roughU.size());
//		System.out.println("FP rate(%)= "+ (double) selected1.size()/roughU.size());
		
		calculateScores("Rough Upper", actual, roughU);
		calculateScores("Dumb ", actual, new HashSet<>(original));
		calculateScores("Rough Lower", actual, roughL);
	}
	
	private void calculateScores(String name, List<Device> actual, Set<Device> select){
		System.out.println();
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> Scores for "+name+" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		Set<Device> relevant = new HashSet<>(actual);
		Set<Device> selected = new HashSet<>(select);
		//calculate fn
		relevant.removeAll(selected);
		System.out.println("FN elements >> "+relevant);
		
		Set<Device> relevant1 = new HashSet<>(actual);
		Set<Device> selected1 = new HashSet<>(select);
		//calculate fp
		selected1.removeAll(relevant1);
		System.out.println("FP elements >> "+selected1);
		
		double p = (double)(actual.size()-relevant.size())/select.size();
		double r = (double)(actual.size()-relevant.size())/actual.size();
		double fscore= 2*p*r/(p+r);
		System.out.println("F score = "+ fscore);
		// % of the identified resources which are false negatives
		System.out.println("FN rate(%)= "+ (double) relevant.size()/actual.size());
		System.out.println("FP rate(%)= "+ (double) selected1.size()/actual.size());
		
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>End scores<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	private List<Device> getActual(List<Device> devicesNoMissing) {
		List<Device> actual = new ArrayList<>();
		
		for (Device device : devicesNoMissing) {
			if (device.getDecision()==DECISION.GOOD){
				actual.add(device);
			}
		}
		return actual;
	}

	Predicate<Device> condition(String key, String value) {
		return d -> d.getMeta().get(key).equals(value);
	}

	private double percentMissing(List<Device> devices){
		
	int missing=0;
	int total=0;
	for (Device d : devices) {
		for (String key : d.getMeta().keySet()) {
			if (isDefined(key, d)) {
				missing++;
			}
			total++;
		}
	}
	return 1.0-(double)missing/total;
}
	private List<Device> getDevicesForORQuery(List<Device> allDevices,
			String query) {
		
		//System.out.println("Invoke OR "+ query);
		List<Device> result = new ArrayList<>();
		if (query.contains("OR")) {
			String[] scopeConditions = query.split("OR");

			for (String andConditions : scopeConditions) {
				
				result.addAll(getDevicesForANDQuery(allDevices, andConditions));
			}
		} else {
			result.addAll(getDevicesForANDQuery(allDevices, query));
		}
		return result;
	}

	private List<Device> getDevicesForANDQuery(List<Device> allDevices,
			String query) {
		
		List<Predicate<Device>> allConditions = new ArrayList<>();
		
		if (query.contains("&")) {
			String[] scopeConditions = query.split("&");

			for (String singleCondition : scopeConditions) {
				String[] tmp = singleCondition.split("=");
				allConditions.add(condition(tmp[0], tmp[1]));
			}
		} else {
		
			String split [] = query.split("=");
			allConditions.add(condition(split[0], split[1]));
		}

		
		Predicate<Device> compositePredicate = 
				allConditions.stream().reduce(
				w -> true, Predicate::and);
	
		List<Device> governanceScope = allDevices.stream()
				.filter(compositePredicate).collect(Collectors.toList());

		return governanceScope;
	}

	private Set<Device> buildUpperApproximation(List<Device> subset,
			List<Device> allDevices, List<String> attrList,
			Map<Block, List<Device>> blocks) {

		Set<Device> union = new HashSet<>();
		for (Device x : allDevices) {
			Set<Device> kbx = buildCaracteristicSet(x, attrList, blocks, allDevices);
			Set<Device> test = new HashSet<>(kbx);
			test.retainAll(subset);
			if (test.size() != 0) {
				union.addAll(kbx);
			}
		}

		return union;
	}
	
	private Set<Device> buildLowerApproximationOptimized(List<Device> subset,
			List<Device> allDevices, List<String> attrList,
			Map<Block, List<Device>> blocks) {

		Set<Device> union = new HashSet<>();
		for (Device x : subset) {
			Set<Device> kbx = buildCaracteristicSet(x, attrList, blocks, allDevices);
			Set<Device> test = new HashSet<>(kbx);
			test.retainAll(subset);
			if (test.size() != 0) {
				union.addAll(kbx);
			}
		}

		return union;
	}

	private Set<Device> buildLowerApproximation(List<Device> subset,
			List<Device> allDevices, List<String> attrList,
			Map<Block, List<Device>> blocks) {

		Set<Device> union = new HashSet<>();
		for (Device x : allDevices) {
			Set<Device> kbx = buildCaracteristicSet(x, attrList, blocks, allDevices);
			if (subset.containsAll(kbx)) {
				union.addAll(kbx);
			}
		}

		return union;
	}

	/**
	 * 
	 * Builds a characteristic set for a device.
	 */
	private Set<Device> buildCaracteristicSet(Device device,
			List<String> attrList, Map<Block, List<Device>> blocks,
			List<Device> allDevices) {
		String id = device.getId();
		// Map<Device,List<Device>> kaSet = new HashMap<>();
		List<Set<Device>> intesectSets = new ArrayList<>();
		for (String attrKey : attrList) {
			if (isDefined(attrKey, device)) {
				intesectSets.add(new HashSet<Device>(blocks.get(new Block(
						attrKey, device.getMeta().get(attrKey)))));
				// System.out.println("Case defined: "+intesectSets);
			} else if ("?".equals(device.getMeta().get(attrKey))
					|| "*".equals(device.getMeta().get(attrKey))) {
				// set.add(U) -- no need since we are intersecting at the end
			} else if ("+".equals(device.getMeta().get(attrKey))) {
				Set<Device> tmp = new HashSet<Device>();
				for (Block b : blocks.keySet()) {
					if (b.getKey().equals(attrKey))
						tmp.addAll(blocks.get(b));
				}
				intesectSets.add(tmp);
				// System.out.println("Case +: "+intesectSets);
			} else if ("-".equals(device.getMeta().get(attrKey))) {
				// union of all devices that satisfy the below condition
				List<Device> tmp = new ArrayList<>();
				for (Device d : allDevices) {
					// same decision
					if (device.getDecision() == d.getDecision()) {
						if (isDefined(attrKey, d)) {
							tmp.add(d);
						}
					}
				}
				Set<Device> union = new HashSet<Device>();
				for (Device v : tmp) {
					Block bl = new Block(attrKey, v.getMeta().get(attrKey));
					union.addAll(blocks.get(bl));
				}
				if (union.size() != 0) {
					intesectSets.add(union);
				} else {
					// add U to intersects -- do nothing since we intersect at
					// the end
				}
				// System.out.println("Case -: "+tmp);

			}
		}

		Set<Device> s = new HashSet<>();
		if (intesectSets.size() > 0) {
			s = intesectSets.get(0);
			for (int i = 1; i < intesectSets.size(); i++) {
				s.retainAll(intesectSets.get(i));
			}
		}

		return s;
	}

	private boolean isDefined(String attrKey, Device device) {
		if (!("?".equals(device.getMeta().get(attrKey))
				|| "+".equals(device.getMeta().get(attrKey))
				|| "-".equals(device.getMeta().get(attrKey)) || "*"
					.equals(device.getMeta().get(attrKey)))) {
			return true;
		}
		return false;
	}

	private Map<Block, List<Device>> handleSpecialValues(
			Map<Block, List<Device>> allblocks, List<Device> allDevices) {

		Map<Block, List<Device>> positives = new HashMap<>();
		Map<Block, List<Device>> negatives = new HashMap<>();

		Iterator<Block> iter = allblocks.keySet().iterator();
		while (iter.hasNext()) {
			Block b = iter.next();
			// List<Device> devs = allblocks.get(b);
			// System.out.println();
			String meta = b.getValue();
			switch (meta) {
			case "?":
				iter.remove();
				break;
			case "+":
			case "*":

				Map<Block, List<Device>> tmp = handlePositive(b,
						allblocks.get(b), allblocks);
				for (Block bl : tmp.keySet()) {
					positives.merge(bl, tmp.get(bl), this.bi);

				}
				// remove the special-value block
				iter.remove();
				break;
			case "-":
				Map<Block, List<Device>> tmp1 = handleNegative(b,
						allblocks.get(b), allblocks, allDevices);
				for (Block bl : tmp1.keySet()) {
					negatives.merge(bl, tmp1.get(bl), this.bi);

				}
				// // remove the special-value block
				iter.remove();
				break;
			default: // normal data
				break;
			}
		}
		if (positives.keySet().size() != 0) {
			for (Block bl : positives.keySet()) {
				allblocks.merge(bl, positives.get(bl), this.bi);
			}
		}
		if (negatives.keySet().size() != 0) {
			for (Block bl : negatives.keySet()) {
				allblocks.merge(bl, negatives.get(bl), this.bi);
			}
		}
		return allblocks;
	}

	private Map<Block, List<Device>> handleNegative(Block key,
			List<Device> devices, final Map<Block, List<Device>> allBlocks,
			List<Device> allDevices) {
		Map<Block, List<Device>> negativeBlocks = new HashMap<>();
		// Expand the block ("key",-)
		// Get all with the same decision as mine
		for (Device me : devices) {
			for (Device device : allDevices) {
				// same decision
				if (me.getDecision() == device.getDecision()) {
					// defined attribute
					if (isDefined(key.getKey(), device)) {
						List<Device> tmp = new ArrayList<>();
						tmp.add(me);
						negativeBlocks.merge(new Block(key.getKey(), device
								.getMeta().get(key.getKey())), tmp, this.bi);
					}
				}
			}

		}
		return negativeBlocks;

	}

	private Map<Block, List<Device>> handlePositive(Block key,
			List<Device> devices, final Map<Block, List<Device>> allBlocks) {
		Map<Block, List<Device>> positiveBlocks = new HashMap<>();
		// Expand the block ("key",+) to ("key","home"), ("key","building"), etc

		for (Block b : allBlocks.keySet()) {
			if (b.getKey().equals(key.getKey())
					&& !("+".equals(b.getValue()) || "*".equals(b.getValue())
							|| "-".equals(b.getValue()) || "?".equals(b
							.getValue()))) {
				positiveBlocks.put(b, devices);
			}
		}
		return positiveBlocks;
	}

	private List<Device> prepareData(List<Device> devicesWithMissingValues,
			String replacement) {
		// FIXME Shellow copy
		// replacement = "location=?&type=+&quality=-"

		Map<String, String> replacements = new HashMap<String, String>();
		String[] keyVal = replacement.split("&");
		for (String datum : keyVal) {
			String[] split = datum.split("=");
			replacements.put(split[0], split[1]);

		}
		List<Device> devicesFilledMeta = new ArrayList<>(
				devicesWithMissingValues);
		for (Device d : devicesFilledMeta) {
			for (String key : d.getMeta().keySet()) {
				if (d.getMeta().get(key).equals("")) {
					d.addMeta(key, replacements.get(key));
				}
			}
		}
		return devicesFilledMeta;

	}

	public Map<Block, List<Device>> makeBlocks(List<Device> devices) {
		// make all possible blocks
		Map<Block, List<Device>> allBlocks = new HashMap<Main.Block, List<Device>>();
		for (Device d : devices) {
			for (String key : d.getMeta().keySet()) {

				allBlocks.merge(new Block(key, d.getMeta().get(key)),
						this.makeList(d), this.bi);
			}
		}
		return allBlocks;

	}

	private List<Device> makeList(Device value) {
		List<Device> l = new ArrayList<>();
		l.add(value);
		return l;
	}

	/**
	 * Pretty printing
	 * 
	 * @param blocks
	 */

	private void printBlocks(Map<Block, List<Device>> blocks) {
		System.out.print(">>>>>>>>>>>>>>>>>> Blocks <<<<<<<<<<<<<<<<<<<\n");
		for (Block b : blocks.keySet()) {
			System.out.printf("%-20s", "\n" + b);
			System.out.printf(" >> ");
			List<Device> devs = blocks.get(b);
			for (Device device : devs) {
				System.out.printf(device.getId() + ", ");
			}
		}

	}

	private void printDataSetAsTable(List<Device> devices) {
		Set<String> attributes = devices.get(0).getMeta().keySet();
		this.pprint("ID");
		for (String attribute : attributes) {
			this.pprint(attribute);
		}
		this.pprint("Decision");
		System.out.println();
		for (Device device : devices) {
			Map<String, String> meta = device.getMeta();
			this.pprint("\n" + device.getId());
			for (String attr : attributes) {
				// String value = meta.get(attr).equals("") ? "?" :
				// meta.get(attr);
				this.pprint(meta.get(attr));
			}
			this.pprint(device.getDecision().toString());
		}

		System.out.println();
		System.out.println();

	}

	private void pprint(String printme) {
		System.out.printf("%-15s", printme);
	}

	/**
	 * 
	 * @author stefan
	 *
	 */
	static class Block {
		String key;
		String value;

		public Block(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "[" + this.key + "," + this.value + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Block other = (Block) obj;
			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}

	public static class Decison {

		private final Device device;
		// Default threshold
		private double threshold = 0.5;

		public enum DECISION {
			GOOD, BAD
		};

		public Decison(Device device, double threshold) {
			this.device = device;
			this.threshold = threshold;
		}

		public DECISION getDecision() {
			if (calculteCompleteness() >= this.threshold)
				return DECISION.GOOD;
			else
				return DECISION.BAD;
		}

		private double calculteCompleteness() {
			int total = device.getMeta().keySet().size();
			int avail = total;
			for (String key : device.getMeta().keySet()) {
				if ("".equals(device.getMeta().get(key)))
					avail--;
			}
			// System.out.println( new Double(avail)/new Double(total));
			return new Double(avail) / new Double(total);
		}

		public void setThreshold(double threshold) {
			this.threshold = threshold;
		}

		public double getThreshold() {
			return threshold;
		}
	}

}
