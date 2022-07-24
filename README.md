# NilChasm

A proof-of-concept mod for NilLoader which (inelegantly) loads CHASM, an ASM transformer library made by QuiltMC. <br/>

### Usage:

- Create `src/main/resources/chasm/<your mod id>/chasm.json`, and include an array of classes to transform, and transformers to use.
	```json
	{
		"classes": [
			"com/mymod/TestTargetClass"
		],
		"transformers": [
			"my_transformer.chasm"
		]
	}
	```

- Create your chasm transformer files and add them to `src/main/resources/chasm/<your mod id>/`. <br/> **EX:** `src/main/resources/chasm/mymod/my_transformer.chasm`

### Building

Use `gradlew shadowJar` to build a jar which contains 