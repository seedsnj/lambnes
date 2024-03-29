package com.lambelly.lambnes.platform.cpu;

import org.apache.log4j.*;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.lambelly.lambnes.platform.cpu.NesCpuAddressingModes;
import com.lambelly.lambnes.platform.Platform;
import com.lambelly.lambnes.util.BitUtils;
import com.lambelly.lambnes.util.NumberConversionUtils;

/**
 *
 * @author thomasmccarthy
 */
public class NesCpu implements CentralProcessingUnit
{	
    // flags
	NesFlags flags = new NesFlags();

	// registers
    private int x = 0;
    private int y = 0;
    private int accumulator = 0;
 
    private NesCpuMemory cpuMemory;
    private NesCpuAddressingModes addressing;

    private Logger logger = Logger.getLogger(NesCpu.class);
    
    public NesCpu()
    {

    }

    public int processNextInstruction()
    {
    	
    	int instCode = cpuMemory.getNextPrgRomByte();
        Instruction instruction = Instruction.get(instCode);
        
        if (instruction == null)
    	{
    		logger.warn("no instruction found for code: " + instCode);
    	}
        
        int value = 0;
    	int address = 0;
    	int x = 0;
    	int y = 0;
    	
        //	logger.debug("program counter: 0x" + Integer.toHexString(cpuMemory.getProgramCounter()) + " -- next instruction: 0x" + Integer.toHexString(instruction.getOpCode()) + "\t|\t" + instruction.name() + "\t|\taddress: " + Integer.toHexString(address) + "\t|\tvalue: " + value + "\t|\tA: " + NumberConversionUtils.generateHexStringWithleadingZeros(this.getAccumulator(),2).toUpperCase() + "\t|\tX: " + NumberConversionUtils.generateHexStringWithleadingZeros(this.getX(),2).toUpperCase() + "\t|\tY: " + NumberConversionUtils.generateHexStringWithleadingZeros(this.getY(),2).toUpperCase() + "\t|\tP: " + NumberConversionUtils.generateHexStringWithleadingZeros(this.getStatus(false),2).toUpperCase());
    	
        switch (instruction.getOpCode())
        {
            /**
                ADC  -  Add to Accumulator with Carry
             */
            case 0x69:
                // #aa
            	value = addressing.getImmediateValue();
            	this.doADC(value);
                break;
            case 0x65:
                // aa
            	value = addressing.getZeroPageValue();
            	this.doADC(value);
            	break;
            case 0x75:
                // aa,X
            	value = addressing.getZeroPageIndexedXValue();
            	this.doADC(value);
            	break;
            case 0x6D:
                // aaaa
            	value = addressing.getAbsoluteValue();
            	this.doADC(value);
            	break;
            case 0x7D:
                // aaaa,X
            	value = addressing.getAbsoluteIndexedXValue();
            	this.doADC(value);
                break;
            case 0x79:
            	// aaaa,Y
            	value = addressing.getAbsoluteIndexedYValue();
            	this.doADC(value);
            	break;
            case 0x61:
            	// (aa,X)
            	value = addressing.getIndexedIndirectXValue();
            	this.doADC(value);            	
            	break;
            case 0x71:
            	// (aa), y
            	value = addressing.getIndirectIndexedYValue();
            	this.doADC(value);            	
            	break;

            /**
             * AND  -  AND Memory with Accumulator
             */
            case 0x29:
                // #aa
                value = addressing.getImmediateValue();
            	this.doAND(value);
                break;
            case 0x25:
                // aa
            	value = addressing.getZeroPageValue();
            	this.doAND(value);
                break;
            case 0x35:
                // aa,X
            	value = addressing.getZeroPageIndexedXValue();
            	this.doAND(value);
            	break;
            case 0x2D:
                // aaaa
            	value = addressing.getAbsoluteValue();
            	this.doAND(value);            	
                break;
            case 0x3D:
                // aaaa,X
            	value = addressing.getAbsoluteIndexedXValue();
            	this.doAND(value);            	
            	break;
            case 0x39:
                // aaaa,Y
            	value = addressing.getAbsoluteIndexedYValue();
            	this.doAND(value);            	            	
                break;
            case 0x21:
                // (aa,X)
            	value = addressing.getIndexedIndirectXValue();
            	this.doAND(value);            	            	          	
                break;
            case 0x31:
                // (aa),Y
            	value = addressing.getIndirectIndexedYValue();
            	this.doAND(value);            	            	          	
                break;

            /**
                ASL  -  Arithmetic Shift Left
             */
            case 0x0A:
                // A
            	value = this.getAccumulator();
            	value = this.doASL(value);
            	this.setAccumulator(value);
                break;
            case 0x06:
                // aa
            	address = addressing.getZeroPageAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doASL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x16:
                // aa,X
            	address = addressing.getZeroPageIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doASL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x0E:
                // aaaa
            	address = addressing.getAbsoluteAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doASL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x1E:
                //aaaa,X
            	address = addressing.getAbsoluteIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doASL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;

            /**
                BCC  -  Branch on Carry Clear
             */
            case 0x90:
                // aa
            	address = addressing.getRelativeAddress(); 
            	if (!this.getFlags().isCarry())
            	{
            		this.doBranch(address);
            	}
                break;

            /**
                BCS  -  Branch on Carry Set
             */
            case 0xB0:
                // aa
            	address = addressing.getRelativeAddress();
            	if (this.getFlags().isCarry())
            	{
            		this.doBranch(address);
            	}
                break;

            /**
                BEQ  -  Branch Zero Set
             */
            case 0xF0:
                // aa
            	address = addressing.getRelativeAddress();
            	if (this.getFlags().isZero())
            	{
            		this.doBranch(address);
            	}
                break;
   
            /**
                BIT  -  Test Bits in Memory with Accumulator
                This instructions is used to test if one or more bits are set in a target memory location. The mask pattern in A is ANDed with the value in memory to set or clear the zero flag, but the result is not kept. Bits 7 and 6 of the value from memory are copied into the N and V flags.
             */
            case 0x24:
                // aa
            	value = addressing.getZeroPageValue();
            	this.doBIT(value);
                break;
            case 0x2C:
                // aaaa
            	value = addressing.getAbsoluteValue();
            	this.doBIT(value);
            	break;

            /**
                BMI  -  Branch on Result Minus
             */
            case 0x30:
                // aa
            	address = addressing.getRelativeAddress();
            	if (this.getFlags().isNegative())
            	{
            		this.doBranch(address);
            	}
                break;

            /**
                BNE  -  Branch on Z reset
             */
            case 0xD0:
                // aa
            	address = addressing.getRelativeAddress();
            	if (!this.getFlags().isZero())
            	{
            		this.doBranch(address);
            	}
                break;
                
            /**
                BPL  -  Branch on Result Plus (or Positive)
             */
            case 0x10:
                // aa
            	address = addressing.getRelativeAddress();
            	if (!this.getFlags().isNegative())
            	{
            		this.doBranch(address);
            	}
                break;

            /**
                BRK  -  Force a Break
             */
            case 0x00:
            	this.pushStatus(true);
                break;
                
            /**
                BVC  -  Branch on Overflow Clear
             */
            case 0x50:
                // aa
            	address = addressing.getRelativeAddress();
            	if (!this.getFlags().isOverflow())
            	{
            		this.doBranch(address);
            	}
                break;

            /**
                BVS  -  Branch on Overflow Set
             */
            case 0x70:
                // aa
            	address = addressing.getRelativeAddress();
            	if (this.getFlags().isOverflow())
            	{
            		this.doBranch(address);
            	}
                break;

            /**
                CLC  -  Clear Carry Flag
             */
            case 0x18:
            	this.getFlags().setCarry(false);
                break;

            /**
                CLD  -  Clear Decimal Mode
             */
            case 0xD8:
            	this.getFlags().setDecimalMode(false);
                break;
                
            /**
                CLI  -  Clear Interrupt Disable
             */
            case 0x58:
            	this.getFlags().setIrqDisable(false);
                break;

            /**
                CLV  -  Clear Overflow Flag
             */
            case 0xB8:
            	this.getFlags().setOverflow(false);
                break;

            /**
                CMP  -  Compare Memory and Accumulator
             */
            case 0xC9:
                // #aa
            	value = addressing.getImmediateValue();
            	this.doCompare(value, this.getAccumulator());
                break;
            case 0xC5:
                // aa
            	value = addressing.getZeroPageValue();
            	this.doCompare(value, this.getAccumulator());
                break;
            case 0xD5:
                // aa,X
            	value = addressing.getZeroPageIndexedXValue();
            	this.doCompare(value, this.getAccumulator());
                break;
            case 0xCD:
                // aaaa
            	value = addressing.getAbsoluteValue();
            	this.doCompare(value, this.getAccumulator());
                break;
            case 0xDD:
                // aaaa,X
            	value = addressing.getAbsoluteIndexedXValue();
            	this.doCompare(value, this.getAccumulator());
                break;
            case 0xD9:
                // aaaa,Y
            	value = addressing.getAbsoluteIndexedYValue();
            	this.doCompare(value, this.getAccumulator());
                break;
            case 0xC1:
                // (aa,X)
            	value = addressing.getIndexedIndirectXValue();
            	this.doCompare(value, this.getAccumulator());
            	break;
            case 0xD1:
                // (aa),Y
            	value = addressing.getIndirectIndexedYValue();
            	this.doCompare(value, this.getAccumulator());
            	break;
                
            /**
                CPX  -  Compare Memory and X register
             */
            case 0xE0:
                // #aa
            	value = addressing.getImmediateValue();
            	this.doCompare(value, this.getX());
                break;
            case 0xE4:
                // aa
            	value = addressing.getZeroPageValue();
            	this.doCompare(value, this.getX());
                break;
            case 0xEC:
                // aaaa
            	value = addressing.getAbsoluteValue();
            	this.doCompare(value, this.getX());
                break;

            /**
                CPY  -  Compare Memory and Y register
             */
            case 0xC0:
                // #aa
            	value = addressing.getImmediateValue();
            	this.doCompare(value, this.getY());
                break;
            case 0xC4:
                // aa
            	value = addressing.getZeroPageValue();
            	this.doCompare(value, this.getY());
                break;
            case 0xCC:
                // aaaa
            	value = addressing.getAbsoluteValue();
            	this.doCompare(value, this.getY());
                break;                
                
            /**
                DEC  -  Decrement Memory by One
             */
            case 0xC6:
                // aa
            	address = addressing.getZeroPageAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doDEC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0xD6:
                // aa,X
            	address = addressing.getZeroPageIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doDEC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;
            case 0xCE:
                // aaaa
            	address = addressing.getAbsoluteAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doDEC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);            
            	break;
            case 0xDE:
                // aaaa,X
            	address = addressing.getAbsoluteIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doDEC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;
                
            /**
                DEX  -  Decrement X
             */
            case 0xCA:
            	x = this.getX();
            	x = Platform.EIGHT_BIT_MASK & --x;
            	
            	this.setX(x);
            	this.checkNegativeBit(x);
            	this.checkZero(x);
                break;

            /**
                DEY  -  Decrement Y
             */
            case 0x88:
            	y = this.getY();
            	y = Platform.EIGHT_BIT_MASK & --y;
            	this.setY(y);
            	this.checkNegativeBit(y);
            	this.checkZero(y);
                break;

            /**
                EOR  -  Exclusive-OR Memory with Accumulator
             */
            case 0x49:
                // #aa
            	value = addressing.getImmediateValue();
            	this.doEOR(value);
                break;
            case 0x45:
                // $aa
            	value = addressing.getZeroPageValue();
            	this.doEOR(value);
                break;
            case 0x55:
                // $aa,X
            	value = addressing.getZeroPageIndexedXValue();
            	this.doEOR(value);
                break;
            case 0x4D:
                // $aaaa
            	value = addressing.getAbsoluteValue();
            	this.doEOR(value);
                break;
            case 0x5D:
                // $aaaa,X
            	value = addressing.getAbsoluteIndexedXValue();
            	this.doEOR(value);
                break;
            case 0x59:
                // $aaaa,Y
            	value = addressing.getAbsoluteIndexedYValue();
            	this.doEOR(value);
                break;
            case 0x41:
                // ($aa,X)
            	value = addressing.getIndexedIndirectXValue();
            	this.doEOR(value);
                break;
            case 0x51:
                // ($aa),Y
            	value = addressing.getIndirectIndexedYValue();
            	this.doEOR(value);
                break;

            /**
                INC  -  Increment Memory by one
             */
            case 0xE6:
                // $aa
            	address = addressing.getZeroPageAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doINC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;
            case 0xF6:
                //  $aa,X
            	address = addressing.getZeroPageIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doINC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;
            case 0xEE:
                // $aaaa
            	address = addressing.getAbsoluteAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doINC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);                
            	break;
            case 0xFE:
                // $aaaa,X
            	address = addressing.getAbsoluteIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doINC(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;

            /**
                INX  -  Increment X by one
             */
            case 0xE8:
            	x = this.getX();
            	x = (++x) & Platform.EIGHT_BIT_MASK;
            	this.setX(x);

            	this.checkNegativeBit(x);
            	this.checkZero(x);
                break;

            /**
                INY  -  Increment Y by one
             */
            case 0xC8:
            	y = this.getY();
            	y = (++y) & Platform.EIGHT_BIT_MASK;
            	this.setY(y);

            	this.checkNegativeBit(y);
            	this.checkZero(y);
            	break;

            /**
                JMP  -  Jump
             */
            case 0x4C:
                // $aaaa
            	address = addressing.getAbsoluteAddress();
            	cpuMemory.setProgramCounter(address);
                break;
            case 0x6C:
                // ($aaaa)
            	address = addressing.getIndirectAbsoluteAddress();
            	cpuMemory.setProgramCounter(address);
            	break;

            /**
                JSR  -  Jump to subroutine
             */
            case 0x20:
                // $aaaa
            	// get address to jump to
            	address = addressing.getAbsoluteAddress();
            	
            	// push current prgRomCounter
            	int a[] = BitUtils.splitAddress(cpuMemory.getProgramCounter() - 1);
            	cpuMemory.pushStack(a[1]);
            	cpuMemory.pushStack(a[0]);
            	
            	//transfer control
            	cpuMemory.setProgramCounter(address);
                break;
                
            /**
                LDA  -  Load Accumulator with memory
             */
            case 0xA9:
                // #aa
            	this.setAccumulator(addressing.getImmediateValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
                break;
            case 0xA5:
                // $aa
            	this.setAccumulator(addressing.getZeroPageValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;
            case 0xB5:
                // $aa,X
            	this.setAccumulator(addressing.getZeroPageIndexedXValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;
            case 0xAD:
                // $aaaa
            	this.setAccumulator(addressing.getAbsoluteValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;
            case 0xBD:
                // $aaaa,X
            	this.setAccumulator(addressing.getAbsoluteIndexedXValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;
            case 0xB9:
                //  $aaaa,Y
            	this.setAccumulator(addressing.getAbsoluteIndexedYValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;
            case 0xA1:
                //  ($aa,X)
            	this.setAccumulator(addressing.getIndexedIndirectXValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;
            case 0xB1:
                // ($aa),Y
            	this.setAccumulator(addressing.getIndirectIndexedYValue());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
            	break;

            /**
                LDX  -  Load X with Memory
             */
            case 0xA2:
                // #aa      
            	this.setX(addressing.getImmediateValue());
            	this.checkNegativeBit(this.getX());
            	this.checkZero(this.getX());
            	break;
            case 0xA6:
                // $aa      
            	this.setX(addressing.getZeroPageValue());
            	this.checkNegativeBit(this.getX());
            	this.checkZero(this.getX());
            	break;
            case 0xB6:
                // $aa,Y    
            	this.setX(addressing.getZeroPageIndexedYValue());
            	this.checkNegativeBit(this.getX());
            	this.checkZero(this.getX());
            	break;
            case 0xAE:
                // $aaaa    
            	this.setX(addressing.getAbsoluteValue());
            	this.checkNegativeBit(this.getX());
            	this.checkZero(this.getX());
            	break;
            case 0xBE:
                // $aaaa,Y  
            	this.setX(addressing.getAbsoluteIndexedYValue());
            	this.checkNegativeBit(this.getX());
            	this.checkZero(this.getX());
            	break;

            /**
                LDY  -  Load Y with Memory
             */
            case 0xA0:
                // #aa    
            	this.setY(addressing.getImmediateValue());
            	this.checkNegativeBit(this.getY());
            	this.checkZero(this.getY());
            	break;
            case 0xA4:
                // $aa
            	this.setY(addressing.getZeroPageValue());
            	this.checkNegativeBit(this.getY());
            	this.checkZero(this.getY());
            	break;
            case 0xB4:
                // $aa,X    
            	this.setY(addressing.getZeroPageIndexedXValue());
            	this.checkNegativeBit(this.getY());
            	this.checkZero(this.getY());
            	break;
            case 0xAC:
                // $aaaa    
            	this.setY(addressing.getAbsoluteValue());
            	this.checkNegativeBit(this.getY());
            	this.checkZero(this.getY());
            	break;
            case 0xBC:
                // $aaaa,X  
            	this.setY(addressing.getAbsoluteIndexedXValue());
            	this.checkNegativeBit(this.getY());
            	this.checkZero(this.getY());
            	break;

            /**
                LSR  -  Logical Shift Right
             */
            case 0x4A:
                // A     
            	value = this.getAccumulator();
            	value = this.doLSR(value);
            	this.setAccumulator(value);
                break;
            case 0x46:
                // $aa
            	address = addressing.getZeroPageAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doLSR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x56:
                // $aa,X    
            	address = addressing.getZeroPageIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doLSR(value);    	
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x4E:
                // $aaaa
            	address = addressing.getAbsoluteAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doLSR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x5E:
                // $aaaa,X  
            	address = addressing.getAbsoluteIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doLSR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;

            /**
                NOP  -  No Operation
             */
            case 0xEA:
                break;

            /**
                ORA  -  OR Memory with Accumulator
             */
            case 0x09:
                // #aa 
            	value = addressing.getImmediateValue();
            	this.doORA(value);
                break;
            case 0x05:
                // $aa
            	value = addressing.getZeroPageValue();
            	this.doORA(value);
                break;
            case 0x15:
                // $aa,X   
            	value = addressing.getZeroPageIndexedXValue();
            	this.doORA(value);
                break;
            case 0x0D:
                // $aaaa   
            	value = addressing.getAbsoluteValue();
            	this.doORA(value);
                break;
            case 0x1D:
                // $aaaa,X 
            	value = addressing.getAbsoluteIndexedXValue();
            	this.doORA(value);
                break;
            case 0x19:
                // $aaaa,Y 
            	value = addressing.getAbsoluteIndexedYValue();
            	this.doORA(value);
                break;
            case 0x01:
                // ($aa,X) 
            	value = addressing.getIndexedIndirectXValue();
            	this.doORA(value);
                break;
            case 0x11:
                // ($aa),Y 
            	value = addressing.getIndirectIndexedYValue();
            	this.doORA(value);
                break;

            /**
                PHA  -  Push Accumulator on Stack
             */
            case 0x48:
            	cpuMemory.pushStack(this.getAccumulator());
                break;
            
            /**
                PHP  -  Push Processor Status on Stack
             */
            case 0x08:
            	this.pushStatus(true);
                break;

            /**
                PLA  -  Pull Accumulator from Stack
             */
            case 0x68:
            	this.setAccumulator(cpuMemory.popStack());
            	this.checkNegativeBit(this.getAccumulator());
            	this.checkZero(this.getAccumulator());
                break;

            /**
                PLP  -  Pull Processor Status from Stack
             */
            case 0x28:
            	this.pullStatus();
                break;

            /**
                ROL  -  Rotate Left
             */
            case 0x2A:
                // A
            	value = this.getAccumulator();
            	this.setAccumulator(this.doROL(value));
                break;
            case 0x26:
                // $aa     
            	address = addressing.getZeroPageAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x36:
                // $aa,X
            	address = addressing.getZeroPageIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x2E:
                // $aaaa   
            	address = addressing.getAbsoluteAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;
            case 0x3E:
                // $aaaa,X 
            	address = addressing.getAbsoluteIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROL(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;

            /**
                ROR  -  Rotate Right
             */
            case 0x6A:
                // A       
            	value = this.getAccumulator();
            	value = this.doROR(value);
            	this.setAccumulator(value);
                break;
            case 0x66:
                // $aa     
            	address = addressing.getZeroPageAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x76:
                // $aa,X   
            	address = addressing.getZeroPageIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x6E:
                // $aaaa   
            	address = addressing.getAbsoluteAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
                break;
            case 0x7E:
                // $aaaa,X 
            	address = addressing.getAbsoluteIndexedXAddress();
            	value = cpuMemory.getMemoryFromHexAddress(address);
            	value = this.doROR(value);
            	cpuMemory.setMemoryFromHexAddress(address, value);
            	break;

            /**
                RTI  -  Return from Interrupt
             */
            case 0x40:
            	// retrieve processor status word
            	this.pullStatus();
            	
            	// program counter
            	int lowByte = cpuMemory.popStack();
            	int highByte = cpuMemory.popStack();	
            	address = BitUtils.unsplitAddress(highByte, lowByte);
            	
            	// transfer control to address
            	cpuMemory.setProgramCounter(address);
                break;

            /**
                RTS  -  Return from Subroutine
             */
            case 0x60:
            	// get address to transfer control to
            	lowByte = cpuMemory.popStack();
            	highByte = cpuMemory.popStack();
            	address = BitUtils.unsplitAddress(highByte, lowByte);
            	
            	// transfer control to address
            	cpuMemory.setProgramCounter(address + 1);
                break;

            /**
                SBC  -  Subtract from Accumulator with Carry
             */
            case 0xE9:
                // #aa
            	value = addressing.getImmediateValue();
            	this.doSBC(value);
                break;
            case 0xE5:
                // $aa
            	value = addressing.getZeroPageValue();
            	this.doSBC(value);
                break;
            case 0xF5:
                // $aa,X   
            	value = addressing.getZeroPageIndexedXValue();
            	this.doSBC(value);
                break;
            case 0xED:
                // $aaaa   
            	value = addressing.getAbsoluteValue();
            	this.doSBC(value);
                break;
            case 0xFD:
                // $aaaa,X 
            	value = addressing.getAbsoluteIndexedXValue();
            	this.doSBC(value);
                break;
            case 0xF9:
                // $aaaa,Y 
            	value = addressing.getAbsoluteIndexedYValue();
            	this.doSBC(value);
                break;
            case 0xE1:
                // ($aa,X) 
            	value = addressing.getIndexedIndirectXValue();
            	this.doSBC(value);
                break;
            case 0xF1:
                // ($aa),Y 
            	value = addressing.getIndirectIndexedYValue();
            	this.doSBC(value);
                break;

            /**
                SEC  -  Set Carry Flag
             */
            case 0x38:
            	this.getFlags().setCarry(true);
                break;

            /**
                SED  -  Set Decimal Mode
             */
            case 0xF8:
            	this.getFlags().setDecimalMode(true);
                break;

            /**
                SEI  -  Set Interrupt Disable
             */
            case 0x78:
            	this.getFlags().setIrqDisable(true);
                break;

            /**
                STA  -  Store Accumulator in Memory
             */
                
            case 0x85:
                // $aa     
            	address = addressing.getZeroPageAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;
            case 0x95:
                // $aa,X  
            	address = addressing.getZeroPageIndexedXAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;
            case 0x8D:
                // $aaaa   
            	address = addressing.getAbsoluteAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;
            case 0x9D:
                // $aaaa,X 
            	address = addressing.getAbsoluteIndexedXAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;
            case 0x99:
                // $aaaa,Y 
            	address = addressing.getAbsoluteIndexedYAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;
            case 0x81:
                // ($aa,X) 
            	address = addressing.getIndexedIndirectXAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;
            case 0x91:
                // ($aa),Y 
            	address = addressing.getIndirectIndexedYAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getAccumulator());
                break;

            /**
                STX  -  Store X in Memory
             */
            case 0x86:
                // $aa   
            	address = addressing.getZeroPageAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getX());
                break;
            case 0x96:
                // $aa,Y 
            	address = addressing.getZeroPageIndexedYAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getX());
            	break;
            case 0x8E:
                // $aaaa 
            	address = addressing.getAbsoluteAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getX());
                break;

            /**
                STY  -  Store Y in Memory
             */
            case 0x84:
                // $aa   
            	address = addressing.getZeroPageAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getY());
                break;
            case 0x94:
                // $aa,X 
            	address = addressing.getZeroPageIndexedXAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getY());
                break;
            case 0x8C:
                // $aaaa 
            	address = addressing.getAbsoluteAddress();
            	cpuMemory.setMemoryFromHexAddress(address, this.getY());
                break;

            /**
                TAX  -  Transfer Accumulator to X
             */
            case 0xAA:
            	this.setX(this.getAccumulator());
            	
            	// check flags
            	this.checkZero(this.getX());
            	this.checkNegativeBit(this.getX());
            	break;

            /**
                TAY  -  Transfer Accumulator to Y
             */
            case 0xA8:
            	this.setY(this.getAccumulator());
            	
            	// check flags
            	this.checkZero(this.getY());
            	this.checkNegativeBit(this.getY());
                break;

            /**
                TSX  -  Transfer Stack to X
             */
            case 0xBA:
            	this.setX(cpuMemory.getStackPointer());
            	
            	// check flags
            	this.checkZero(this.getX());
            	this.checkNegativeBit(this.getX());
                break;

            /**
                TXA  -  Transfer X to Accumulator
             */
            case 0x8A:
            	this.setAccumulator(this.getX());
            	
            	// check flags
            	this.checkZero(this.getAccumulator());
            	this.checkNegativeBit(this.getAccumulator());
                break;

            /**
                TXS  -  Transfer X to Stack pointer
             */
            case 0x9A:
            	cpuMemory.setStackPointer(this.getX());
                break;

            /**
                TYA  -  Transfer Y to Accumulator
             */
            case 0x98:
            	this.setAccumulator(this.getY());
            	
            	// check flags
            	this.checkZero(this.getAccumulator());
            	this.checkNegativeBit(this.getAccumulator());            	
                break;
            
            /**
             * default -- only encountered for unemulated instructions.
             */
            default:
            	throw new IllegalStateException("ENCOUNTERED UNEMULATED INSTRUCTION: " + Integer.toHexString(instruction.getOpCode()));
        }
        
        return instruction.getCycles();
    }
    
    private void doADC(int value)
    {
    	// A,Z,C,N = A+M+C
    	int result = this.getAccumulator() + value + (this.getFlags().isCarry()?1:0);
    	
    	// check carry flag
    	// When the addition result is 0 to 255, the carry is cleared. 
    	// When the addition result is greater than 255, the carry is set. 
    	if (result >=0 && result <= 255)
    	{
    		this.getFlags().setCarry(false);
    	}
    	else if (result > 255)
    	{
    		this.getFlags().setCarry(true);
    	}
    	
       	// check overflow
    	// when result outside -128 to 127 set overflow
    	int twosComplimentResult = (byte)value + (byte)this.getAccumulator() + (this.getFlags().isCarry()?1:0);
    	this.getFlags().setOverflow(twosComplimentResult > 127 || twosComplimentResult < -128);
    	
    	result = result & Platform.EIGHT_BIT_MASK;
    	
    	// check negative and zero flags
    	this.checkNegativeBit(result);
    	this.checkZero(result);
    	
    	this.setAccumulator(result);
    }
    
    private void doAND(int value)
    {
        this.setAccumulator(this.getAccumulator() & value);

        // check flags
        this.checkNegativeBit(this.getAccumulator());
        this.checkZero(this.getAccumulator());    	
    }
    
    private int doASL(int value)
    {
    	// set Carry
    	this.getFlags().setCarry(BitUtils.isBitSet(value, 7));
    	
    	// do shift
    	value = value << 1;
    	value = value & Platform.EIGHT_BIT_MASK;
    	
    	// check flags
    	this.checkNegativeBit(value);
    	this.checkZero(value);    	
    	
    	return value;
    }
    
    private void doBIT(int value)
    {	
    	// and acc and set zero
    	int maskedValue = this.getAccumulator() & value;
    	this.checkZero(maskedValue);
    	
    	// set N and V
    	this.checkNegativeBit(value);
    	this.getFlags().setOverflow(BitUtils.isBitSet(value, 6));
    }
    
    private void doBranch(int address)
    {
    	cpuMemory.setProgramCounter(address);
    }
    
    private void doCompare(int value, int register)
    {
    	// set C -- set if acc is greater than or equal to value
    	if (register >= value)
    	{
    		this.getFlags().setCarry(true);
    	}
    	else
    	{
    		this.getFlags().setCarry(false);
    	}
    	
    	// set N -- most significant bit of unsigned subtraction result
    	int sub = ((register - value) & Platform.EIGHT_BIT_MASK);
    	this.getFlags().setNegative(BitUtils.isBitSet(sub, 7));
    	
    	// set Z -- comparison result
    	if (register == value)
    	{
    		this.getFlags().setZero(true);
    	}
    	else
    	{
    		this.getFlags().setZero(false);
    	}
    }
    
    private int doDEC(int value)
    {
    	// decrement
    	value--;
    	
    	// bit mask
    	value = value & Platform.EIGHT_BIT_MASK;
    	
    	// check flags
    	this.checkNegativeBit(value);
    	this.checkZero(value);   
    	
    	return value;
    	
    }
    
    private void doEOR(int value)
    {
        this.setAccumulator(this.getAccumulator() ^ value);

        // check flags
        this.checkNegativeBit(this.getAccumulator());
        this.checkZero(this.getAccumulator()); 
    }
    
    private int doINC(int value)
    {
    	// increment
    	value++;
    	
    	// bit mask
    	value = value & Platform.EIGHT_BIT_MASK;
    	
    	// check flags
    	this.checkNegativeBit(value);
    	this.checkZero(value);   
    	
    	return value;
    	
    }
    
    private int doLSR(int value)
    {
    	// set carry
    	this.getFlags().setCarry(BitUtils.isBitSet(value, 0));

    	// do shift
    	value = value >> 1;
    	
    	// check flags
    	this.checkNegativeBit(value);
    	this.checkZero(value);
    	
    	return value;
    }
    
    private void doORA(int value)
    {
        this.setAccumulator(this.getAccumulator() | value);

        // check flags
        this.checkNegativeBit(this.getAccumulator());
        this.checkZero(this.getAccumulator()); 
    }
    
    private int doROL(int value)
    {
    	//Move each of the bits in either A or M one place to the left. Bit 0 is filled with the current value of the carry flag whilst the old bit 7 becomes the new carry flag value.
    	// get new carry -- bit 7 of 
    	boolean newCarry = BitUtils.isBitSet(value,7);
    	
    	// do shift
    	value = (value << 1) & Platform.EIGHT_BIT_MASK;
    	
    	// swap carry with bit 0
    	if (this.getFlags().isCarry())
    	{
    		value = BitUtils.setBit(value, 0);
    	}
    	else
    	{
    		value = BitUtils.unsetBit(value, 0);
    	}
    	this.getFlags().setCarry(newCarry);
    	
    	// check flags
        this.checkNegativeBit(value);
        this.checkZero(value);
    	
        return value;
    }
    
    private int doROR(int value)
    {
    	//Move each of the bits in either A or M one place to the right. Bit 7 is filled with the current value of the carry flag whilst the old bit 0 becomes the new carry flag value.
    	boolean newCarry = BitUtils.isBitSet(value,0);
    	
    	// do shift
    	value = (value >> 1) & Platform.EIGHT_BIT_MASK;
    	
    	// swap carry with bit 7
    	if (this.getFlags().isCarry())
    	{
    		value = BitUtils.setBit(value, 7);
    	}
    	else
    	{
    		value = BitUtils.unsetBit(value, 7);
    	}
    	
    	this.getFlags().setCarry(newCarry);
    	
    	// check flags
        this.checkNegativeBit(value);
        this.checkZero(value);
    	
        return value;
    }    
    
    private void doSBC(int value)
    {
    	// A-M-(1-C)
    	int result = this.getAccumulator() - value - (1 - (this.getFlags().isCarry()?1:0)); 
    	this.checkZero(result);
    	this.checkNegativeBit(result);
    	
       	// check overflow
    	// when two's compliment result outside -128 to 127 set overflow
    	int twosComplimentResult = (byte)this.getAccumulator() - (byte)value - (1 - (this.getFlags().isCarry()?1:0));
    	this.getFlags().setOverflow(twosComplimentResult > 127 || twosComplimentResult < -128);
    	
    	// check carry 
    	// When the subtraction result is 0 to 255, the carry is set. 
    	// When the subtraction result is less than 0, the carry is cleared. 
    	if (result >= 0 && result <= 255)
    	{
    		this.getFlags().setCarry(true);
    	}
    	else
    	{
    		this.getFlags().setCarry(false);
    	}
    	
    	this.setAccumulator(result & Platform.EIGHT_BIT_MASK);
    }    
        
    /**
     * checks bit 7 of the accumulator. If set, set N flag.
     */
    private void checkNegativeBit(int value)
    {
    	if (BitUtils.isBitSet(value, 7))
        {
            this.getFlags().setNegative(true);
        }
        else
        {
            this.getFlags().setNegative(false);
        }
    }

    private void checkZero(int value)
    {
    	this.getFlags().setZero(value == 0);
    }
    
    public void resetRegisters()
    {
    	this.setAccumulator(0);
    	this.setX(0);
    	this.setY(0);
    }
    
    public void pushStatus(boolean brk)
    {
    	int status = this.getStatus(brk);
    	cpuMemory.pushStack(status);
    }
    
    public void pullStatus()
    {
    	int status = cpuMemory.popStack();
    	this.getFlags().setNegative(BitUtils.isBitSet(status, 7));
    	this.getFlags().setOverflow(BitUtils.isBitSet(status, 6));
    	this.getFlags().setDecimalMode(BitUtils.isBitSet(status, 3));
    	this.getFlags().setIrqDisable(BitUtils.isBitSet(status, 2));
    	this.getFlags().setZero(BitUtils.isBitSet(status, 1));
    	this.getFlags().setCarry(BitUtils.isBitSet(status, 0));
    }
    
    public int getStatus(boolean brk)
    {
    	// http://wiki.nesdev.com/w/index.php/CPU_status_flag_behavior 
    	int status = ((this.getFlags().isNegative()?1:0) << 7) |
    		((this.getFlags().isOverflow()?1:0) << 6) |
    		(1 << 5) |
    		((brk?1:0) << 4) |
    		((this.getFlags().isDecimalMode()?1:0) << 3) | 
    		((this.getFlags().isIrqDisable()?1:0) << 2) |
    		((this.getFlags().isZero()?1:0) << 1) | 
    		(this.getFlags().isCarry()?1:0);
    	
    	return status;
    }
    
    public String toString()
    {
    	return "X: " + this.getX() + "\n" +
    		"Y: " + this.getY() + "\n" +
    		"A: " + this.getAccumulator() + "\n" +
    	    "program counter: 0x" + Integer.toHexString(cpuMemory.getProgramCounter());
    }

    /**
     * @return the x
     */
    public int getX()
    {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x)
    {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY()
    {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y)
    {
        this.y = y;
    }

    /**
     * @return the accumulator
     */
    public int getAccumulator()
    {
        return accumulator;
    }

    /**
     * @param accumulator the accumulator to set
     */
    public void setAccumulator(int accumulator)
    {
        this.accumulator = accumulator;
    }
    
    public NesFlags getFlags()
	{
		return flags;
	}

	public void setFlags(Flags flags)
	{
		this.flags = (NesFlags)flags;
	}

	public NesCpuMemory getCpuMemory()
    {
    	return cpuMemory;
    }

	public void setCpuMemory(NesCpuMemory cpuMemory)
    {
    	this.cpuMemory = cpuMemory;
    }

	public NesCpuAddressingModes getAddressing()
    {
    	return addressing;
    }

	public void setAddressing(NesCpuAddressingModes addressing)
    {
    	this.addressing = addressing;
    }    
}

