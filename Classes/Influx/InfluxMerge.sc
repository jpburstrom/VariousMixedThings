InfluxMerge {
	var <inValDict, <outValDict, <>mergeFunc, <>damping = 0.5;
	var <trusts;

	*initClass {
		Class.initClassTree(Halo);
		this.addSpec(\damping, [0, 1]);
	}

	*new {
		^super.new.init;
	}
	init {
		inValDict = ();
		outValDict = ();
		trusts = ();

		// damping 0 is linear sum of contribs,
		// damping 0.5 is scaled by sqrt of contribs (equal power sum?)
		// damping 1 is linear average

		mergeFunc = { |in, out, trusts, damping = 0.5|
			in.keysValuesDo { |key, values|
				var outval = 0;
				values.keysValuesDo { |srcName, val|
					var contrib = val * (trusts[srcName]);
					outval = outval + contrib;
				};
				outval = outval / (values.size ** damping);
				out.put(key, outval);
			}
		}
	}

	influence {|who ... keyValPairs|
		keyValPairs.pairsDo { |param, val|
			if (inValDict[param].isNil) { inValDict[param] = () };
			inValDict[param].put(who, val);
		};
		this.checkWeights(who);
		mergeFunc.value(inValDict, outValDict, trusts);
	}

	checkWeights { |who|
		if (trusts[who].isNil) { trusts[who] = 1 };
	}

	trust { |srcName, val|
		trusts.put(srcName, val);
		mergeFunc.value(inValDict, outValDict, trusts, damping);
	}
}
