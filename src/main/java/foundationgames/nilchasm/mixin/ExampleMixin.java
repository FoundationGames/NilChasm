package foundationgames.nilchasm.mixin;

import foundationgames.nilchasm.chasmix.test.TestTargetClass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TestTargetClass.class)
public class ExampleMixin {
    @Final @Shadow @Mutable
    private String myField;

    public int newField;

    @Inject(method = "myStaticMethod", at = @At("HEAD"))
    private static void ghjkl(CallbackInfo ci) {
        System.out.println("foo");
    }
}
